package de.fu_berlin.inf.dpp.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.NullProgressMonitor;

public class BlockingProgressMonitor extends NullProgressMonitor {

    protected CountDownLatch latch;

    public BlockingProgressMonitor() {
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void done() {
        this.latch.countDown();
    }

    public void await() throws InterruptedException {
        this.latch.await();
    }

    public boolean await(long timeout) throws InterruptedException {
        return this.latch.await(timeout, TimeUnit.SECONDS);
    }
}
