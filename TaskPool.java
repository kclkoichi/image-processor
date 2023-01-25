package com.kcl.osc.imageprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class TaskPool implements Runnable {

    // number of maximum running threads
    private int size;

    // actual number of running threads
    private int threadsCount;

    // waiting list of tasks
    private LinkedList<ImageProcessorMT> waitingList;

    // actually running tasks
    private ArrayList<ImageProcessorMT> runningList;

    /**
     * A thread pool that will manage the execution of threads.
     * @param size The size of the thread pool
     */
    public TaskPool(int size) {
        this.size = size;
        threadsCount = 0;
        waitingList = new LinkedList<>();
        runningList = new ArrayList<>();
    }

    /**
     * Adds a task (ImageProcessorMT) to the waiting list.
     * @param task the ImageProcessorMT
     */
    public void submit(ImageProcessorMT task) {
        waitingList.add(task);
    }

    /**
     * Starts the thread pool.
     * Runs as many ImageProcessorMTs as it can up to its size.
     */
    public void start() {
        this.run();
    }

    /**
     * Runs all the tasks in the thread pool.
     * It terminates when all tasks have been executed and terminated.
     */
    @Override
    public void run() {

        // Going to run all tasks in the waiting list
        while(!waitingList.isEmpty()) {
            // Starting new task
            new Thread(waitingList.peek()).start();
            runningList.add(waitingList.pop());
            threadsCount += 1;

            // Checking for tasks that has ended if maximum capacity of running threads is reached
            while(threadsCount == size) {
                tryRemoveFinishedTasks();
            }
        }

        // Now that the waiting list is empty,
        // we need to wait that all remaining tasks (in the running list) terminate
        while(!runningList.isEmpty()) {
            tryRemoveFinishedTasks();
        }

    }

    /**
     * Removes finished tasks from the running list,
     * none is removed if none has finished.
     */
    private void tryRemoveFinishedTasks() {
        Iterator<ImageProcessorMT> it = runningList.iterator();
        while(it.hasNext()) {
            if(it.next().hasEnded()) {
                it.remove();
                threadsCount -= 1;
            }
        }
    }

    /**
     * Causes the calling thread to wait until
     * all tasks in the thread pool have terminated
     */
    public void join() throws InterruptedException {
        while (!waitingList.isEmpty() && !runningList.isEmpty()) {
            // Just wait that all tasks in the pool terminate
        }
    }

}
