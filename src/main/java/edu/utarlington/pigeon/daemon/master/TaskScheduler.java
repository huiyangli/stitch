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

import edu.utarlington.pigeon.daemon.scheduler.Scheduler;
import edu.utarlington.pigeon.daemon.util.Network;
import edu.utarlington.pigeon.daemon.util.Utils;
import edu.utarlington.pigeon.thrift.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A TaskScheduler is a buffer that holds task reservations until an application backend is
 * available to run the task. When a backend is ready, the TaskScheduler requests the task
 * from the {@link Scheduler} that submitted the reservation.
 * <p>
 * Each scheduler will implement a different policy determining when to launch tasks.
 * <p>
 * Schedulers are required to be thread safe, as they will be accessed concurrently from
 * multiple threads.
 */
public abstract class TaskScheduler {

    protected class TaskSpec {
        public String appId;
        public TUserGroupInfo user;
        public String requestId;

        public InetSocketAddress schedulerAddress;
        public InetSocketAddress appBackendAddress;

        //Used by recursive service: notify the scheduler master node completed the request identified by requestId
        public THostPort master;

        /**
         * ID of the task that previously ran in the slot this task is using. Used
         * to track how long it takes to fill an empty slot on a slave. Empty if this task was launched
         * immediately, because there were empty slots available on the slave.  Filled in when
         * the task is launched.
         */
        public String previousRequestId;
        public String previousTaskId;

        /** Filled in after the getTask() RPC completes. */
        /**
         * For pigeon, taskSpec is filled in when nm get the launch task request
         */
        public TTaskLaunchSpec taskSpec;

        /**
         * Used to construct a dummy reservation to stimulate TaskLaunchService notify Pigeon scheduler the tasks for the request are completed
         */
        public TaskSpec(String appId, String requestId, InetSocketAddress schedulerAddr) {
            this.appId = appId;
            this.requestId = requestId;
            this.schedulerAddress = schedulerAddr;
        }

        public TaskSpec(String appId, String previousRequestId, String previousTaskId, THostPort schedulerAddress, InetSocketAddress appBackendAddress) {
            this.appId = appId;
            this.previousRequestId = previousRequestId;
            this.previousTaskId = previousTaskId;
            this.schedulerAddress = Network.thriftToSocketAddress(schedulerAddress);
            this.appBackendAddress = appBackendAddress;
        }

        public TaskSpec(TLaunchTasksRequest request, InetSocketAddress appBackendAddress) {
            appId = request.getAppID();
            user = request.getUser();
            requestId = request.getRequestID();
            taskSpec = unWrapLaunchTaskRequest(request);
            schedulerAddress = new InetSocketAddress(request.getSchedulerAddress().getHost(),
                    request.getSchedulerAddress().getPort());
            this.appBackendAddress = appBackendAddress;
            previousRequestId = "";
            previousTaskId = "";
        }
    }

    private final static Logger LOG = Logger.getLogger(TaskScheduler.class);
    protected String ipAddress;

    protected Configuration conf;

    /**
     * High/low priority task list
     */

//    protected ArrayList<BlockingQueue<TaskSpec>> workerTaskQueue = new ArrayList<BlockingQueue<TaskSpec>>();
    protected HashMap<Double, Long> key2LaunchedTimeStamp;
    protected HashMap<Double, Long> key2EnqueueTimeStamp;
    private final BlockingQueue<TaskSpec> runnableTaskQueue =
            new LinkedBlockingQueue<TaskSpec>();

    /**
     * Initialize the task scheduler, passing it the current available resources
     * on the machine.
     */
    void initialize(Configuration conf) {
        this.conf = conf;
        this.ipAddress = Network.getIPAddress(conf);
        key2LaunchedTimeStamp = new HashMap<Double, Long>();
        key2EnqueueTimeStamp = new HashMap<Double, Long>();
    }

    /**
     * Get the next task available for launching. This will block until a task is available.
     */
    TaskSpec getNextTask(boolean collectPerfMetrix) {
        TaskSpec task = null;
        try {
            task = runnableTaskQueue.take();
        } catch (InterruptedException e) {
            LOG.fatal(e);
        }

        if (collectPerfMetrix) {
            double key = Utils.hashCode(task.requestId, task.taskSpec.taskId);
            key2LaunchedTimeStamp.put(key, System.currentTimeMillis());
            LOG.debug("Adding key: " + key + " for request_" + task.requestId + " task_" + task.taskSpec.taskId);
        }

        return task;
    }

    /**
     * Returns the current number of runnable tasks (for testing).
     */
//    int runnableTasks() {
//        return runnableTaskQueue.size();
//    }

//    boolean tasksFinished(List<TFullTaskId> finishedTasks, InetSocketAddress backendAddress, Integer workerId) {
//        boolean isIdle = false;
//        for (TFullTaskId t : finishedTasks) {
////            AUDIT_LOG.info(Logging.auditEventString("task_completed", t.getRequestId(), t.getTaskId()));
//             isIdle = handleTaskFinished(t.getAppId(), t.getRequestId(), t.getTaskId(), t.getSchedulerAddress(), backendAddress, workerId);
//        }
//        return isIdle;
//    }

    void noTaskForReservation(String appId, String requestId, InetSocketAddress schedulerAddr, THostPort master) {
        handleNoTasksReservations(appId, requestId, schedulerAddr, master);
    }

    protected void makeTaskRunnable(TaskSpec task) {
        try {
            LOG.debug("Make task_" + task.taskSpec.taskId + " for request_" + task.requestId + " runnable!");
            runnableTaskQueue.put(task);
        } catch (InterruptedException e) {
            LOG.fatal("Unable to add task to runnable queue: " + e.getMessage());
        }
    }

    public long getTaskQueueingTimeMS(String requestId, String taskId) {
        double key = Utils.hashCode(requestId, taskId);
        if (!key2EnqueueTimeStamp.containsKey(key)) return -1;

        long taskLaunchedTimeStamp = key2LaunchedTimeStamp.get(key);
        long taskEnqueueTimeStamp = key2EnqueueTimeStamp.get(key);
        LOG.debug("Get queueing time for task_" + taskId + "of request: " + requestId + " ,launchedTimeStamp: " + taskLaunchedTimeStamp + " , enqueuedTimeStamp: " + taskEnqueueTimeStamp);
        return taskLaunchedTimeStamp - taskEnqueueTimeStamp;
    }

    public long getTaskLaunchedTimeStamp(String requestId, String taskId) {
        double key = Utils.hashCode(requestId, taskId);
//        LOG.debug("Generate key value for request_" + requestId + " task_" + taskId + " key is: " + key);
        if (key2LaunchedTimeStamp.isEmpty() || !key2LaunchedTimeStamp.containsKey(key))
            throw new IllegalStateException("Something went wrong, key: " + key + " doesn't exist for  task_" + taskId + "for request_" + requestId + " doesn't exist in cache");

        return key2LaunchedTimeStamp.get(key);
    }


    public void clear(String requestId, String taskId) {
        double key = Utils.hashCode(requestId, taskId);

        key2LaunchedTimeStamp.remove(key);
        key2EnqueueTimeStamp.remove(key);
    }


//    public synchronized void submitLaunchTaskRequest(TLaunchTasksRequest request,
//                                                    InetSocketAddress appBackendAddress) {
//        TaskSpec taskToBeLaunched = new TaskSpec(request, appBackendAddress);
//        LOG.debug("Launching task_" + taskToBeLaunched.taskSpec.taskId + " for request: " + request.requestID + " at time stamp " + System.currentTimeMillis());
//
//        double key = Utils.hashCode(request.requestID, request.tasksToBeLaunched.get(0).taskId);
//        key2LaunchedTimeStamp.put(key, System.currentTimeMillis());
//        handleSubmitTaskLaunchRequest(taskToBeLaunched);
//    }

    private TTaskLaunchSpec unWrapLaunchTaskRequest(TLaunchTasksRequest request) {
        if (request.tasksToBeLaunched != null && request.tasksToBeLaunched.size() == 1)
            return request.tasksToBeLaunched.get(0);
        else {//TODO: Handling more than one tasks
            LOG.debug("Fetching more than one tasks for the request.");
            return null;
        }
    }

    // TASK SCHEDULERS MUST IMPLEMENT THE FOLLOWING.

    /**
     * Handles a task reservation. Returns the number of queued reservations.
     */
//    abstract int handleSubmitTaskReservation(TaskSpec taskReservation);

    /**
     * Handles the launch task request, returns the number of tasks to be launched (or ... reservations?)
     * @param taskToBeLaucnhed
     * @return
     */
//    abstract void handleSubmitTaskLaunchRequest(TaskSpec taskToBeLaucnhed);

    /**
     * Cancels all task reservations with the given request id. Returns the number of task
     * reservations cancelled.
     */

    /**
     * Handles the completion of a task that has finished executing.
     */
//    protected abstract boolean handleTaskFinished(String requestId, String taskId, THostPort schedulerAddress, InetSocketAddress backendAddress);
    protected abstract boolean handleTaskFinished(String appId, String requestId, String taskId, THostPort schedulerAddress, InetSocketAddress backendAddress, Integer workerId);

    /**
     * Handles the case when the node monitor tried to launch a task for a reservation, but
     * the corresponding scheduler didn't return a task (typically because all of the corresponding
     * job's tasks have been launched).
     */
    protected abstract void handleNoTasksReservations(String appId, String requestId, InetSocketAddress schedulerAddr, THostPort master);

    /**
     * Returns the maximum number of active tasks allowed (the number of workers).
     * <p>
     * -1 signals that the scheduler does not enforce a maximum number of active tasks.
     */
    protected abstract int getWorkersPerMaster();

    /**
     * Enqueue the task to its corresponding worker queue
     */
    protected abstract void enqueue(TLaunchTasksRequest launchTasksRequest, int workerID, InetSocketAddress workerAddr, boolean collectPerfMetrics);

//    public abstract void registerBackend(InetSocketAddress backendAddr);
}
