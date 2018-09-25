package de.fu_berlin.inf.dpp.test.util;

/**
 * A Thread which runs the give Runnable. After the thread finished, you might
 * call verify to ensure that no exceptions where thrown during execution of the
 * given Runnable.
 * 
 * TODO: This is a copy of 'TestThread' in the core project. One could either
 * export that class from the core bundle as well, or provide it in a separate
 * bundle to be used for STF tests.
 */

public class EclipseTestThread extends Thread {

    public interface Runnable {
        public void run() throws Exception;
    }

    volatile Error error;
    volatile Exception exception;
    volatile boolean finished = false;

    private java.lang.Runnable runnableToRun;
    private Runnable testThreadRunnable;

    public EclipseTestThread(java.lang.Runnable runnable) {
        this.runnableToRun = runnable;
        this.setName("Test-Thread:" + runnable.toString());
    }

    public EclipseTestThread(Runnable runnable) {
        this.testThreadRunnable = runnable;
        this.setName("Test-Thread:" + runnable.toString());
    }

    @Override
    public void run() {
        try {
            if (runnableToRun != null)
                runnableToRun.run();

            if (testThreadRunnable != null)
                testThreadRunnable.run();

        } catch (Exception e) {
            exception = e;
        } catch (Error e) {
            error = e;
        } finally {
            finished = true;
        }
    }

    public void verify() throws Exception {

        if (!finished)
            throw new IllegalStateException(this.getName()
                + " is still running or has not been started at all");

        if (error != null)
            throw error;

        if (exception != null)
            throw exception;
    }
}