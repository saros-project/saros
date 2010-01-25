package de.fu_berlin.inf.dpp.net.test;

import java.util.concurrent.BlockingQueue;

import junit.framework.AssertionFailedError;

/**
 * A Thread which puts all Exceptions and AssertionFailedErrors caught into the
 * given BlockingQueue
 */
public class TestThread extends Thread {

    public TestThread(final BlockingQueue<Throwable> failures,
        final Runnable runnable) {
        super(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    failures.add(e);
                } catch (AssertionFailedError e) {
                    failures.add(e);
                }
            }
        });
    }
}