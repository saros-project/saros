/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;
import java.util.Set;

/**
 * This class helps to interrupt blocking operations by external Threads. Just
 * wrap the operation with
 * 
 * <pre>
 * try {
 *     try {
 *         threadAccessRecorder.record();
 *     } catch (InterruptedException e) {
 *         // threadAccessRecorder already interrupted
 *     }
 *     longRunningAndInterruptibleOperation();
 * } catch (InterruptedException e) {
 *     // threadAccessRecorder or thread interrupted
 * } finally {
 *     try {
 *         threadAccessRecorder.release();
 *     } catch (InterruptedException e) {
 *         // handle optionally
 *     }
 * }
 * </pre>
 * 
 * and when the recorder gets interrupted (by
 * {@link ThreadAccessRecorder#interrupt()}) it interrupts all recorded Threads
 * to stop these operations.
 * 
 * @author s-lau
 */
public class ThreadAccessRecorder {
    protected Set<Thread> threads = new HashSet<Thread>();
    protected boolean interrupted = false;

    /**
     * After this call a blocking or long operation starts. {@link #release()}
     * <strong>MUST</strong> be called after this operation finished.
     * 
     * @throws InterruptedException
     *             when this recorder was told to interrupt and can not record
     *             anymore
     */
    public synchronized void record() throws InterruptedException {
        if (!interrupted)
            threads.add(Thread.currentThread());
        else
            throw new InterruptedException();
    }

    /**
     * Operation finished.
     * 
     * @throws InterruptedException
     *             when this recorder got interrupted but operation was already
     *             done (or doesn't respect the interruption)
     */
    public synchronized void release() throws InterruptedException {
        threads.remove(Thread.currentThread());
        // re-interrupt when we're not interruptor
        if (Thread.interrupted() && !interrupted)
            Thread.currentThread().interrupt();
        if (interrupted)
            throw new InterruptedException();
    }

    /**
     * Interrupts all Threads which are recorded but not yet released.
     * Afterwards calls to {@link #record()} or {@link #release()} will lead to
     * {@link InterruptedException}s.
     */
    public synchronized void interrupt() {
        interrupted = true;
        for (Thread thread : threads) {
            thread.interrupt();
        }
        threads.clear();
    }

    public synchronized boolean interrupted() {
        return interrupted;
    }

}