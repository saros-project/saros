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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;

/**
 * Abstract {@link Job} for using {@link StreamSession}s. It contains a set up
 * {@link StreamSessionListener} which sets adequate fields and cancels the
 * running {@link Job} when notified.
 * 
 * @author s-lau
 */
public abstract class StreamJob extends Job {

    private static Logger log = Logger.getLogger(StreamJob.class);

    /**
     * {@link #streamSession} had an exception
     */
    protected volatile StreamException streamException;
    /**
     * {@link #streamSession} was told to stop
     */
    protected volatile boolean stopped = false;
    /**
     * {@link #streamSession}s listener
     */
    protected StreamJobListener streamJobListener;
    private StreamSession streamSession;

    /**
     * Barrier to shutdown session when work is done
     */
    protected CountDownLatch readyToStop = new CountDownLatch(1);

    protected StreamJob(String name) {
        super(name);
        this.streamJobListener = new StreamJobListener();
    }

    /**
     * Stores session and adds {@link StreamJobListener}
     * 
     * @param streamSession
     *            to be used for this job
     */
    protected void setStreamSession(StreamSession streamSession) {
        this.streamSession = streamSession;
        streamSession.setListener(streamJobListener);
    }

    /**
     * @return session for this job
     */
    protected StreamSession getStreamSession() {
        return streamSession;
    }

    /**
     * The listener for {@link StreamSession} in {@link StreamJob}. The job is
     * canceled when an error and shutdown is triggered and sets appropriate
     * fields in {@link StreamJob}.
     */
    protected class StreamJobListener implements StreamSessionListener {

        public void errorOccured(StreamException e) {
            StreamJob.this.streamException = e;
            StreamJob.this.cancel();
        }

        public void sessionStopped() {
            StreamJob.this.stopped = true;
            Utils.runSafeAsync(log, new Runnable() {
                public void run() {
                    StreamJob.this.cancel();
                    try {
                        StreamJob.this.readyToStop.await(30, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        log.error("Not designed to be interrupted");
                        Thread.currentThread().interrupt();
                    }
                    if (StreamJob.this.streamSession != null)
                        StreamJob.this.streamSession.shutdownFinished();
                }
            });

        }

    }
}