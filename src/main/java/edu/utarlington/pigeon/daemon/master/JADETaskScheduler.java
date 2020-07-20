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

import edu.utarlington.pigeon.daemon.util.Network;
import edu.utarlington.pigeon.daemon.util.ThriftClientPool;
import edu.utarlington.pigeon.daemon.util.Utils;
import edu.utarlington.pigeon.thrift.RecursiveService;
import edu.utarlington.pigeon.thrift.THostPort;
import edu.utarlington.pigeon.thrift.TLaunchTasksRequest;
import edu.utarlington.pigeon.thrift.TTaskLaunchSpec;
//import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.apache.thrift.async.AsyncMethodCallback;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This scheduler assumes that backends can execute a fixed number of tasks (equal to
 * the number of cores on the machine) and uses a FIFO queue to determine the order to launch
 * tasks whenever outstanding tasks exceed this amount.
 */
public class FifoTaskScheduler extends TaskScheduler {
    private final static Logger LOG = Logger.getLogger(FifoTaskScheduler.class);

    public int maxActiveTasks;

    //    /** Thrift client pool for communicating with Pigeon scheduler */
    ThriftClientPool<RecursiveService.AsyncClient> recursiveClientPool =
            new ThriftClientPool<RecursiveService.AsyncClient>(
                    new ThriftClientPool.RecursiveServiceMakerFactory());

    /** Available workers passed from master, should be invoked only at startup of master and this scheduler */
    public FifoTaskScheduler(int numOfvCPUs) {
        maxActiveTasks = numOfvCPUs;
    }

    @Override
    protected void handleNoTasksReservations(String appId, String requestId, InetSocketAddress scheduler, THostPort master) {
        InetSocketAddress schedulerAddress = Network.constructSocket(scheduler, 20507);
        try {
            RecursiveService.AsyncClient recursiveClient = recursiveClientPool.borrowClient(schedulerAddress);
            LOG.debug("Notifying the scheduler all tasks for request " + requestId + " have completed.");
            recursiveClient.tasksFinished(requestId, master, new TasksFinishedCallBack(requestId, schedulerAddress) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TasksFinishedCallBack
            implements AsyncMethodCallback<RecursiveService.AsyncClient.tasksFinished_call> {
        InetSocketAddress schedulerAddr;
        String requestId;
        long startTimeMillis;

        public TasksFinishedCallBack(String requestId, InetSocketAddress schedulerAddr) {
            this.requestId = requestId;
            this.schedulerAddr = schedulerAddr;
            this.startTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void onComplete(RecursiveService.AsyncClient.tasksFinished_call response) {
            try {
                long totalTime = System.currentTimeMillis() - startTimeMillis;
                LOG.debug( "Scheduler: " + schedulerAddr + " has been notified that all tasks from request: " + requestId);
                recursiveClientPool.returnClient(schedulerAddr, (RecursiveService.AsyncClient) response.getClient());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    @Override
    protected int getMaxActiveTasks() {
        return maxActiveTasks;
    }

    @Override
    protected synchronized void enqueue(TLaunchTasksRequest task, int workerID, InetSocketAddress workerAddr, boolean collectPerfMetrics) {
        TTaskLaunchSpec taskSpec = task.tasksToBeLaunched.get(0);
        LOG.debug("Enqueue task_" + taskSpec.taskId + " for request: " + task.requestID + " at time stamp: " + System.currentTimeMillis());

        TaskSpec t = new TaskSpec(task, workerAddr);
        workerTaskQueue.get(workerID).add(t); //enqueue

        if (collectPerfMetrics) {
            double key = Utils.hashCode(task.requestID, taskSpec.taskId);
            key2EnqueueTimeStamp.put(key, System.currentTimeMillis());
        }
    }
}
