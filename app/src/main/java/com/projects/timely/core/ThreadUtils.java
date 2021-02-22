package com.projects.timely.core;

/**
 * Set of utility methods to be used to efficiently manage Thread execution.
 */
public class ThreadUtils {

    /**
     * Utility method to inline background threads with normal program execution
     *
     * @param target the task to be run
     */
    public static void runBackgroundTask(Runnable target) {
        Thread thread = new Thread(target);
        thread.start();
    }
}
