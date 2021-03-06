/*
 * PIGEON
 * Copyright 2018 Univeristy of Texas at Arlington
 *
 * Modified from Sparrow - University of California, Berkeley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package edu.utarlington.pigeon.daemon.master;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.utarlington.pigeon.daemon.PigeonConf;
import edu.utarlington.pigeon.daemon.util.Network;
import edu.utarlington.pigeon.daemon.util.Serialization;
import edu.utarlington.pigeon.daemon.util.ThriftClientPool;
import edu.utarlington.pigeon.daemon.util.Utils;
import edu.utarlington.pigeon.thrift.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Pigeon master which is responsible for communicating with application
 * backends and scheduler. This class is wrapped by multiple thrift servers, so it may
 * be concurrently accessed when handling multiple function calls
 * simultaneously.
 * <p>
 * 1) It maintain lists of available workers
 * <p>
 * 2) It delegates the assignments of requests to taskScheduler
 */
public class PigeonMaster {

    /**
     * Used to uniquely identify addr registered with this master.
     */
    private AtomicInteger workercnt = new AtomicInteger(0);
    private final static Logger LOG = Logger.getLogger(PigeonMaster.class);

    private static PigeonMasterState state;

    private static final boolean COLLECT_PERFORMANCE_METRICS = false;

    //* --- Thread-Safe Fields --- *//
    // A map to workers, filled in when backends register themselfs with their master
    private HashMap<String, List<WorkerWithId>> appSockets;
    //dictionary keep record of worker addr ==> id
    private HashMap<InetSocketAddress, Integer> workerAddr2WorkerId;
    InetSocketAddress[] workerId2WorkerAddr;
    //A map of occupied workers, keyed by the addr's ID, the value is the addr socket
//    private HashMap<InetSocketAddress, Integer> occupiedWorkers;
    //Record of number of tasks from the same requests
    private ConcurrentMap<String, Integer> requestNumberOfTasks =
            Maps.newConcurrentMap();
    // Record the maximum turn-arround time for all the dispatched tasks for certain request_id
    private HashMap<String, Long> requestElapsedTime;
    //* --- Thread-Safe Fields (END)--- *//

    // Map to scheduler socket address for each request id.
    private ConcurrentMap<String, InetSocketAddress> requestSchedulers =
            Maps.newConcurrentMap();

    private ThriftClientPool<SchedulerService.AsyncClient> schedulerClientPool =
            new ThriftClientPool<SchedulerService.AsyncClient>(
                    new ThriftClientPool.SchedulerServiceMakerFactory());

    private TaskScheduler scheduler;
    private TaskLauncherService taskLauncherService;
    private String ipAddress;
    private int masterInternalPort;

    public void initialize(Configuration conf, int masterInternalPort) {
        String mode = conf.getString(PigeonConf.DEPLYOMENT_MODE, "unspecified");
        int wokersPerMaster = conf.getInt(PigeonConf.WORKERS_PER_MASTER, 4);
        if (mode.equals("standalone")) {
            //TODO: Other mode
        } else if (mode.equals("configbased")) {
            state = new ConfigMasterState();
        } else {
            throw new RuntimeException("Unsupported deployment mode: " + mode);
        }
        try {
            state.initialize(conf);
        } catch (IOException e) {
            LOG.fatal("Error initializing node monitor state.", e);
        }

        ipAddress = Network.getIPAddress(conf);
        this.masterInternalPort = masterInternalPort;

        String task_scheduler_type = conf.getString(PigeonConf.NM_TASK_SCHEDULER_TYPE, "fifo");
        if (task_scheduler_type.equals("round_robin")) {
//            scheduler = new RoundRobinTaskScheduler(cores);
        } else if (task_scheduler_type.equals("fifo")) {
            scheduler = new JADETaskScheduler(wokersPerMaster);
        } else if (task_scheduler_type.equals("priority")) {
//            scheduler = new PriorityTaskScheduler(cores);
        } else {
            throw new RuntimeException("Unsupported task scheduler type: " + mode);
        }

        /** Initialize addr list for Pigeon master */
        appSockets = new HashMap<String, List<WorkerWithId>>();
        workerAddr2WorkerId = new HashMap<InetSocketAddress, Integer>();
        workerId2WorkerAddr = new InetSocketAddress[wokersPerMaster];
        requestElapsedTime = new HashMap<String, Long>();

        /** Initialize of book-keeping of swapped workers */
//        occupiedWorkers = new HashMap<InetSocketAddress, Integer>();

        /** Initialize task scheduler & task launcher service */
        scheduler.initialize(conf);
        taskLauncherService = new TaskLauncherService();
        taskLauncherService.initialize(conf, scheduler, COLLECT_PERFORMANCE_METRICS);
    }

    public boolean registerBackend(String app, InetSocketAddress internalAddr, InetSocketAddress backendAddr, int type) {
        LOG.debug("Attempt to register addr: " + backendAddr + " at master:" + internalAddr + " for App: " + app);
        //TODO: fix backend registration synchonization problem
        int id = workercnt.getAndIncrement();
        workerAddr2WorkerId.put(backendAddr, id);
        workerId2WorkerAddr[id] = backendAddr;
//        scheduler.registerBackend(backendAddr);

        WorkerWithId worker = new WorkerWithId(id, backendAddr);
        if (!appSockets.containsKey(app))
            appSockets.put(app, Lists.newArrayList(worker));
        else {
            List<WorkerWithId> workers = appSockets.get(app);
            if (!workers.contains(worker))
                workers.add(worker);
        }

        LOG.debug("debuginfo2 " + appSockets.get(app).size());
        //TODO: verify the backend matches with the configured information
        return state.registerBackend(app, internalAddr, backendAddr, type);
    }

    public boolean launchTasksRequest(TLaunchTasksRequest request) throws TException {
        LOG.info("Received launch task request from " + ipAddress + " for request " + request.requestID);
        requestElapsedTime.put(request.requestID, System.currentTimeMillis());

        InetSocketAddress schedulerAddress = new InetSocketAddress(request.getSchedulerAddress().getHost(), 20503);
        requestSchedulers.put(request.getRequestID(), schedulerAddress);

        synchronized (state) {
            List<WorkerWithId> workers = appSockets.get(request.appID);

            //short-circuit to check if app backends (workers) have been started, o.w. throw out exceptions
            if (!state.masterNodeUp()) {
                if (workers.isEmpty()) {
                    throw new ServerNotReadyException("Master node must have more than one high/low priority addr available at start by default.");
                }
            }

            requestNumberOfTasks.put(request.requestID, request.tasksToBeLaunched.size());
        }

        for (int i = 0; i < request.tasksToBeLaunched.size(); i++) {
            TTaskLaunchSpec task = request.tasksToBeLaunched.get(i);
            InetSocketAddress workerAddr = workerId2WorkerAddr[i];

            //enqueue
            scheduler.enqueue(new TLaunchTasksRequest(request.appID, request.user, request.requestID, request.schedulerAddress, Lists.newArrayList(task)), i, workerAddr, COLLECT_PERFORMANCE_METRICS);
        }
        return true;
    }

    public void taskFinishedInMS(List<TFullTaskId> task, THostPort worker, long elapsed) {
        InetSocketAddress idleWorker = Network.thriftToSocketAddress(worker);
        int idleWorkerId = workerAddr2WorkerId.get(idleWorker);

        String app = task.get(0).appId;
        String requestId = task.get(0).requestId;
        String taskId = task.get(0).taskId;
        LOG.debug("Task_" + taskId + " for request_" + requestId + " has completed in " + elapsed + " ms");

        //record metrics
        if (COLLECT_PERFORMANCE_METRICS) {
            long taskElapsedTimeTotal = System.currentTimeMillis() - scheduler.getTaskLaunchedTimeStamp(requestId, taskId);
            long taskQueueingTimeMs = scheduler.getTaskQueueingTimeMS(requestId, taskId) == -1 ? 0 : scheduler.getTaskQueueingTimeMS(requestId, taskId);
//            scheduler.clear(requestId, taskId);
            LOG.debug("Request:" + requestId + "'s Task: " + task.get(0).taskId + " has completed in " + taskElapsedTimeTotal + " ms, in which queueing time is : " + taskQueueingTimeMs + " ms");
            long taskNetworkingTimeEst = (taskElapsedTimeTotal - elapsed) / 2;
            String metricInfo = "Request: " + requestId +
                    " Task: " + task.get(0).taskId +
                    " TotalTaskElapsedMS " + taskElapsedTimeTotal +
                    " WorkerTaskElapsedMS " + elapsed +
                    " QueueingTimeMs " + taskQueueingTimeMs +
                    " networkingMS: " + taskNetworkingTimeEst;
            Utils.writeToLocalFile("network-metrics.info", metricInfo);
        }

        synchronized (state) {
            countTaskReservations(app, requestId);
        }

        scheduler.handleTaskFinished(app, requestId, taskId, task.get(0).schedulerAddress, idleWorker, idleWorkerId);
    }

    //Count the number of tasks finished for particular request; if so, handle the situation
    private void countTaskReservations(String appId, String requestId) {
        int counter = requestNumberOfTasks.get(requestId);
        counter--;
        if (counter == 0) {
            requestNumberOfTasks.remove(requestId);
            //If all tasks from the same request finished, inform the Pigeon scheduler
            scheduler.noTaskForReservation(appId, requestId, requestSchedulers.get(requestId), getMasterInternalSocket());
            requestSchedulers.remove(requestId);

            //record request finished time
            Long startTime = requestElapsedTime.get(requestId);
            Long endTime = System.currentTimeMillis();
            Long latency = endTime - startTime;
            String requestInfo = "Request: " + requestId + " exec latency: " + latency + " ms";
            LOG.debug(requestInfo);
            //save local
            Utils.writeToLocalFile("RequestInfoMaster.txt", requestInfo);
        } else
            requestNumberOfTasks.put(requestId, counter);
    }

    private THostPort getMasterInternalSocket() {
        InetSocketAddress socket = Serialization.strToSocket(ipAddress + ":" + String.valueOf(masterInternalPort)).get();
        return Network.socketAddressToThrift(socket);
    }

    private class sendFrontendMessageCallback implements
            AsyncMethodCallback<SchedulerService.AsyncClient.sendFrontendMessage_call> {
        private InetSocketAddress frontendSocket;
        private SchedulerService.AsyncClient client;

        public sendFrontendMessageCallback(InetSocketAddress socket, SchedulerService.AsyncClient client) {
            frontendSocket = socket;
            this.client = client;
        }

        public void onComplete(SchedulerService.AsyncClient.sendFrontendMessage_call response) {
            try {
                schedulerClientPool.returnClient(frontendSocket, client);
            } catch (Exception e) {
                LOG.error(e);
            }
        }

        public void onError(Exception exception) {
            try {
                schedulerClientPool.returnClient(frontendSocket, client);
            } catch (Exception e) {
                LOG.error(e);
            }
            LOG.error(exception);
        }
    }

    public void sendFrontendMessage(String app, TFullTaskId taskId, int status, ByteBuffer message) {
        InetSocketAddress scheduler = requestSchedulers.get(taskId.requestId);
        if (scheduler == null) {
            LOG.error("Did not find any scheduler info for request: " + taskId);
            return;
        }

        try {
            SchedulerService.AsyncClient client = schedulerClientPool.borrowClient(scheduler);
            client.sendFrontendMessage(app, taskId, status, message,
                    new sendFrontendMessageCallback(scheduler, client));
            LOG.debug("finished sending message to frontend!");
        } catch (IOException e) {
            LOG.error(e);
        } catch (TException e) {
            LOG.error(e);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private class WorkerWithId {
        InetSocketAddress addr;
        int id;

        public WorkerWithId(int pid, InetSocketAddress pAddr) {
            id = pid;
            addr = pAddr;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((addr == null) ? 0 : addr.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            WorkerWithId other = (WorkerWithId) obj;
            if (addr == null) {
                if (other.addr != null)
                    return false;
            } else if (!addr.equals(other.addr))
                return false;

            if (id != other.id) {
                return false;
            }

            return true;
        }
    }
}

