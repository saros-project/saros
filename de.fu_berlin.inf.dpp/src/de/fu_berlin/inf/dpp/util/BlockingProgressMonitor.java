package de.fu_berlin.inf.dpp.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

/**
 * A ProgressMonitor which can be waited upon. </p> <s> Typical usage pattern:
 * 
 * 
 * 
 * <pre>
 * BlockingProgressMonitor blockingMonitor = new BlockingProgressMonitor(monitor);
 * object.longRunningOperation(blockingMonitor);
 * 
 * try {
 *     blockingMonitor.await();
 * } catch (InterruptedException e) {
 *     log.error(&quot;Code not designed to be interruptible&quot;, e);
 *     Thread.currentThread().interrupt();
 * }
 * </pre>
 * 
 * </s>
 * 
 * Please use only the {@link #await(long)} method of that class.
 * 
 * @deprecated
 */

@Deprecated
public class BlockingProgressMonitor extends ProgressMonitorWrapper {

    protected CountDownLatch latch;

    public BlockingProgressMonitor() {
        this(new NullProgressMonitor());
    }

    /**
     * Will delegate all calls to the given ProgressMonitor (can still maintain
     * the possibility to wait on the completion of the task).
     */
    public BlockingProgressMonitor(IProgressMonitor delegate) {
        super(delegate);
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void done() {
        super.done();
        this.latch.countDown();
    }

    @Override
    public void setCanceled(boolean cancelled) {
        super.setCanceled(cancelled);
        if (cancelled) {
            this.latch.countDown();
        }
    }

    /**
     * @deprecated Can cause deadlocks or infinitive waits if the corresponding
     *             logic fails to call {@link #done()} on the given monitor.
     * 
     * @throws InterruptedException
     */
    @Deprecated
    public void await() throws InterruptedException {
        this.latch.await();
    }

    public boolean await(long timeout) throws InterruptedException {
        return this.latch.await(timeout, TimeUnit.SECONDS);
    }
}
