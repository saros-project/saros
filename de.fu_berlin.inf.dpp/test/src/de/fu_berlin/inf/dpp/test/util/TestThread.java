package de.fu_berlin.inf.dpp.test.util;

import java.util.concurrent.BlockingQueue;

import junit.framework.AssertionFailedError;

// FIXME: Is this run by TestRunners?
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