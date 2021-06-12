package com.noah.timely.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Set of utility methods to be used to efficiently manage Thread execution.
 */
public class ThreadUtils {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Utility method to inline background threads with normal program execution
     *
     * @param target the task to be run
     */
    public static void runBackgroundTask(Runnable target) {
        executorService.execute(target);
    }

    /**
     * Shutdown all tasks
     */
    public static void shutdownAllTasks() {
        executorService.shutdown();
    }
}
