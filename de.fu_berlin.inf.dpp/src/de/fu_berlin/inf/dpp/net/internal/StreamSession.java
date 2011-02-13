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
package de.fu_berlin.inf.dpp.net.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.picocontainer.Disposable;

import com.Ostermiller.util.CircularByteBuffer;

import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager.StreamPacket;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager.StreamPath;
import de.fu_berlin.inf.dpp.util.ThreadAccessRecorder;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This is a concrete session, based on a {@link StreamService}. It contains all
 * available streams and methods to handle the life-cycle of this session.
 * 
 * @author s-lau
 */
public class StreamSession implements Disposable {

    private static Logger log = Logger.getLogger(StreamSession.class);

    protected StreamServiceManager streamServiceManager;
    protected StreamSessionListener sessionListener = null;

    /**
     * Service of this session
     */
    protected StreamService basedService;

    /**
     * The path for this session to send meta-packets
     */
    protected StreamPath streamMetaPath;

    /**
     * Unique session ID (in combination with initiator)
     */
    protected int sessionID;

    /**
     * who started this session
     */
    protected JID initiator;

    /**
     * who receives our data etc.
     */
    protected JID remoteJID;

    /**
     * initial object passed with the negotiation
     */
    protected Object initiationDescription;

    /**
     * All available {@link InputStream}s
     */
    protected StreamSessionInputStream[] inputStreams;
    /**
     * All available {@link OutputStream}s
     */
    protected StreamSessionOutputStream[] outputStreams;

    /**
     * For each {@link StreamSessionOutputStream} a possible thread which sleeps
     * {@link StreamService#getMaximumDelay()} and then forces the data to be
     * send. It is started when a stream contains less data than it's defined
     * chunk-size.
     * 
     * @see StreamService#getChunkSize()
     */
    protected Thread[] resendThread;
    protected Runnable shutdown = null;

    /**
     * will be set to <code>true</code> when receiver send a STOPPED-packet (his
     * shutdown finished).
     */
    protected boolean receiverStopped = false;

    /**
     * We stopped this session, acknowledged shutdown by
     * {@link #shutdownFinished()}
     */
    protected boolean stopped = false;

    protected boolean disposed = false;

    /**
     * Create a new {@link StreamSession}
     * 
     * @param service
     *            this session is based on
     * @param receiver
     *            remote peer of this session (who receives the data)
     * @param initiator
     *            user who initiated this session
     * @param initial
     *            can be <code>null</code>
     */
    protected StreamSession(StreamServiceManager streamServiceManager,
        StreamService service, JID receiver, JID initiator, int sessionID,
        Object initial) {
        this.remoteJID = receiver;
        this.initiator = initiator;
        this.sessionID = sessionID;
        this.basedService = service;
        this.streamMetaPath = new StreamPath(initiator, getService(), sessionID);
        this.streamServiceManager = streamServiceManager;
        this.initiationDescription = initial;
        this.resendThread = new Thread[basedService.getStreamsPerSession()];

        // setup streams
        int streams = service.getStreamsPerSession();
        inputStreams = new StreamSessionInputStream[streams];
        outputStreams = new StreamSessionOutputStream[streams];

        for (int i = 0; i < streams; i++) {
            int bufferSize = basedService.getBufferSize()[i];

            inputStreams[i] = new StreamSessionInputStream(i, bufferSize);
            outputStreams[i] = new StreamSessionOutputStream(i, bufferSize);

        }
    }

    public StreamService getService() {
        return basedService;
    }

    /**
     * 
     * @return a META-path for this session
     */
    protected StreamPath getStreamPath() {
        return streamMetaPath;
    }

    /**
     * @return a {@link TransferDescription} for sending meta-packets to
     *         {@link #remoteJID}
     */
    protected TransferDescription getTransferDescription() {
        return TransferDescription.createStreamMetaTransferDescription(
            remoteJID, streamServiceManager.saros.getMyJID(), getStreamPath()
                .toString(), streamServiceManager.sarosSessionID.getValue());
    }

    public JID getRemoteJID() {
        return remoteJID;
    }

    /**
     * Access to the {@link OutputStream}'s of this session
     * 
     * @param streamID
     *            ID of desired stream
     * @return stream or <code>null</code> if there's no such stream with given
     *         ID
     */
    public OutputStream getOutputStream(int streamID) {
        return streamID < 0 || streamID >= getService().getStreamsPerSession() ? null
            : outputStreams[streamID];
    }

    /**
     * Access to the {@link InputStream}'s of this session
     * 
     * @param streamID
     *            ID of desired stream
     * @return stream or <code>null</code> if there's no such stream with given
     *         ID
     */
    public InputStream getInputStream(int streamID) {
        return streamID < 0 || streamID >= getService().getStreamsPerSession() ? null
            : inputStreams[streamID];
    }

    /**
     * 
     * @return sessionID of this session
     */
    public int getSessionID() {
        return this.sessionID;
    }

    /**
     * Terminates this running session. The receiver will be notified. After
     * that the registered listener's method
     * {@link StreamSessionListener#sessionStopped()} will be called to shut our
     * session down.
     */
    public void stopSession() {
        streamServiceManager.stopSession(this);

        if (sessionListener == null)
            // immediately stop when no listener registered
            shutdownFinished();
    }

    /**
     * closes the session and it's streams
     */
    public synchronized void dispose() {
        if (disposed)
            return;
        disposed = true;
        // close streams
        closeStreams(inputStreams);
        closeStreams(outputStreams);
        // shutdown threads
        for (Thread t : resendThread) {
            if (t != null)
                t.interrupt();
        }
        if (streamServiceManager.sender != null)
            streamServiceManager.sender.removeData(this);
        streamServiceManager.sessions.remove(this.getStreamPath());
    }

    private void closeStreams(Stream[] streams) {
        for (Stream s : streams) {
            s.closedByInternal();
        }
    }

    /**
     * Notifies our {@link #streamServiceManager} that we are finished with
     * shutdown. Should only be called after a stop request
     * {@link StreamSessionListener#sessionStopped()}.
     */
    public void shutdownFinished() {
        streamServiceManager.stoppedSession(this);
        this.stopped = true;
    }

    /**
     * 
     * @return <code>true</code> when we are initiator of this session
     */
    public boolean isInitiator() {
        return this.initiator.equals(streamServiceManager.saros.getMyJID());
    }

    /**
     * @return The {@link Object} passed with the initiation for this session.
     */
    public Object getInitiationDescription() {
        return initiationDescription;
    }

    @Override
    public String toString() {
        return remoteJID + "[" + initiator + "-" + sessionID + "] ("
            + getService().getServiceName() + ")";
    }

    /**
     * Register a listener to this session. There can only be one listener at a
     * time. If session is already stopped when listener is added, it will be
     * notified. If session is disposed or stopped when listener is added, an
     * error is notified.
     */
    public void setListener(StreamSessionListener listener) {
        sessionListener = listener;

        if (sessionListener != null) {
            if (stopped)
                sessionListener.sessionStopped();
        }
    }

    protected void addPacket(StreamPacket packet) {
        int streamID = packet.getStreamPath().streamID;
        if (streamID >= getService().getStreamsPerSession()) {
            log.error("Received packet for unknown streamID #" + streamID
                + "! " + getService() + " contains only "
                + getService().getStreamsPerSession());
            return;
        }

        inputStreams[streamID].addPacket(packet);
    }

    /**
     * Reports an error to sessions listener and disposes this session.
     * 
     * @param e
     */
    protected void reportErrorAndDispose(StreamException e) {
        if (sessionListener != null)
            sessionListener.errorOccured(e);
        this.dispose();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((initiator == null) ? 0 : initiator.hashCode());
        result = prime * result + sessionID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StreamSession other = (StreamSession) obj;
        if (initiator == null) {
            if (other.initiator != null)
                return false;
        } else if (!initiator.equals(other.initiator))
            return false;
        if (sessionID != other.sessionID)
            return false;
        return true;
    }

    /**
     * This listener provides call-backs for termination of a session. A session
     * can be terminated by either an explicit stop-request of one participant
     * or an error.
     */
    public interface StreamSessionListener {

        /**
         * A session shutdown was requested (by remote or ourself). This means
         * we have to shutdown this session now within
         * {@link StreamServiceManager#SESSION_SHUTDOWN_LIMIT} and call
         * {@link StreamSession#shutdownFinished()} to signal finished shutdown.
         */
        public void sessionStopped();

        /**
         * Report an error in the session/connection. This session will be
         * disposed when this call returns.
         * 
         * @param e
         *            The exception which caused an error
         */
        public void errorOccured(StreamException e);
    }

    /**
     * Getter and closing methods for streams, no matter they are in or out.
     */
    interface Stream extends Closeable {
        /**
         * 
         * @return {@link StreamSession} this stream belongs to
         */
        public StreamSession getSession();

        /**
         * Getter for streams ID
         * 
         * @return
         */
        public int streamID();

        /**
         * Other side of this stream is closed. This means that this stream is
         * now closed as well, because it makes no sense to leave it open.
         */
        public void closedByRemote();

        /**
         * Closes the stream without notifying related one (at receiver). Should
         * only be used internally.
         */
        public void closedByInternal();

        /**
         * 
         * @return <code>true</code> when stream is closed, <code>false</code>
         *         otherwise
         */
        public boolean isClosed();
    }

    /**
     * Virtual {@link OutputStream} for sending data to related
     * {@link StreamSessionInputStream} at {@link StreamSession#remoteJID}.
     */
    public class StreamSessionOutputStream extends OutputStream implements
        Stream {
        // TODO include SubMonitor!?

        protected ByteArrayOutputStream output;
        protected int streamID;
        /**
         * Related {@link StreamSessionInputStream} is closed
         */
        protected boolean closedByRemote = false;
        /**
         * this stream is closed
         */
        protected boolean closed = false;
        /**
         * Remaining buffer in this stream. Limits the capacity of
         * {@link #output}.
         */
        protected Semaphore freeBuffer;
        protected int bufferSize;

        protected ThreadAccessRecorder threadAccessRecorder = new ThreadAccessRecorder();

        protected StreamSessionOutputStream(int streamID, int bufferSize) {
            super();
            this.streamID = streamID;
            this.output = new ByteArrayOutputStream(bufferSize);
            this.freeBuffer = new Semaphore(bufferSize, true);
            this.bufferSize = bufferSize;
        }

        public void closedByRemote() {
            closedByRemote = true;
        }

        @Override
        public void write(int b) throws IOException {
            checkState();

            try {
                threadAccessRecorder.record();
                freeBuffer.acquire(1);
                synchronized (output) {
                    output.write(b);
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }

            notifyStreamServiceManager(false);
        }

        /**
         * Check state of stream and throw {@link IOException} when necessary
         * 
         * @throws IOException
         *             when this stream itself or sink is is closed
         * @see #close()
         */
        protected void checkState() throws IOException {
            if (closed)
                throw new IOException("This stream is closed.");
            if (closedByRemote)
                throw new IOException("Sink has been closed.");
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed)
                return;
            streamServiceManager.closeStream(this);
            closedByInternal();
        }

        public void closedByInternal() {
            if (closed)
                return;
            this.threadAccessRecorder.interrupt();
            closed = true;
            // only close empty buffer
            if (output.size() == 0)
                disposeBuffer();
        }

        protected void disposeBuffer() {
            if (output == null)
                return;

            try {
                output.close();
            } catch (IOException e) {
                // ignore
            }
            output = null;
        }

        @Override
        public void flush() throws IOException {
            checkState();
            output.flush();
            notifyStreamServiceManager(true);
        }

        @Override
        public void write(byte[] arg0) throws IOException {
            checkState();

            try {
                threadAccessRecorder.record();
                freeBuffer.acquire(arg0.length);
                synchronized (output) {
                    output.write(arg0);
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }

            notifyStreamServiceManager(false);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkState();

            try {
                threadAccessRecorder.record();
                freeBuffer.acquire(len - off);
                synchronized (output) {
                    output.write(b, off, len);
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }

            notifyStreamServiceManager(false);
        }

        /**
         * Gets the data this stream contains. The returned data will be removed
         * from this stream.
         * 
         * @param removeAllAvailableData
         *            get any amount of data without restrictions streams
         *            minimal chunk-size
         * @return the data in this stream, <code>null</code> when none
         *         available
         * @see StreamService#getChunkSize()
         */
        protected synchronized byte[] getData(boolean removeAllAvailableData) {
            if (output == null)
                return null;
            synchronized (output) {
                int data_length = output.size();
                if (data_length == 0)
                    // no data, nothing to do
                    return null;
                if (!removeAllAvailableData) {
                    // check if we should send data
                    if (data_length < StreamSession.this.getService()
                        .getChunkSize()[streamID]) {
                        // not enough data, spawn resendThread and return
                        if (StreamSession.this.resendThread[streamID] != null)
                            // another one waits already
                            return null;
                        StreamSession.this.resendThread[streamID] = Utils
                            .runSafeAsync(StreamSession.this.toString()
                                + "-dataWaiter-" + streamID, log,
                                new Runnable() {
                                    public void run() {
                                        try {
                                            Thread
                                                .sleep(StreamSession.this
                                                    .getService()
                                                    .getMaximumDelay()[streamID]);
                                            notifyStreamServiceManager(true);
                                        } catch (InterruptedException e) {
                                            // stop execution
                                            return;
                                        } finally {
                                            StreamSession.this.resendThread[streamID] = null;
                                        }
                                    }
                                });
                        return null;
                    }
                }
                byte[] data = output.toByteArray();
                log.trace("Will read " + Utils.formatByte(data.length));
                output.reset();
                if (StreamSession.this.resendThread[streamID] != null)
                    StreamSession.this.resendThread[streamID].interrupt();
                freeBuffer.release(data.length);

                if (closed)
                    disposeBuffer();

                return data;
            }
        }

        /**
         * Reports that data was written to this stream. The
         * {@link StreamSession#streamServiceManager} will be notified.
         * 
         * @param sendAllAvailableData
         *            when <code>true</code> any data in this stream will be
         *            send without restrictions.
         */
        protected void notifyStreamServiceManager(boolean sendAllAvailableData) {
            streamServiceManager.notifyDataAvailable(this,
                sendAllAvailableData, null);
        }

        public StreamSession getSession() {
            return StreamSession.this;
        }

        public int streamID() {
            return streamID;
        }

        protected StreamPath getStreamPath(int size) {
            return new StreamPath(initiator, sessionID, streamID, size);
        }

        public int getFreeBuffer() {
            return freeBuffer.availablePermits();
        }

        public int getTotalBuffer() {
            return bufferSize;
        }

        public boolean isClosed() {
            return closed || closedByRemote;
        }

    }

    /**
     * Virtual {@link InputStream} for accessing arrived data in this
     * {@link StreamSession}. When related {@link StreamSessionOutputStream} is
     * closed (at {@link StreamSession#remoteJID}), remaining bytes can be read
     * until end of stream is signaled.
     */
    public class StreamSessionInputStream extends InputStream implements Stream {
        // TODO include SubMonitor!?

        /**
         * Incoming packets which are not written to {@link #buffer} yet
         */
        protected Queue<StreamServiceManager.StreamPacket> remainingPackets = new LinkedList<StreamServiceManager.StreamPacket>();

        /**
         * Buffers data this stream contains
         */
        protected CircularByteBuffer buffer;

        protected int streamID;

        /**
         * Related {@link StreamSessionOutputStream} is closed
         */
        protected boolean closedByRemote = false;

        /**
         * this stream is closed
         */
        protected boolean closed = false;

        protected int readBytes = 0;

        protected ThreadAccessRecorder threadAccessRecorder = new ThreadAccessRecorder();

        protected StreamSessionInputStream(int streamID, int bufferSize) {
            this.streamID = streamID;
            // +10 because it uses some space for internal markers
            this.buffer = new CircularByteBuffer(bufferSize + 10, true);

        }

        public void closedByRemote() {
            closedByRemote = true;
            try {
                if (remainingPackets.isEmpty() && available() == 0)
                    threadAccessRecorder.interrupt();
            } catch (IOException e) {
                threadAccessRecorder.interrupt();
            }
        }

        /**
         * 
         * @return Read bytes since start of stream or last call to this method.
         */
        public synchronized int getReadBytes() {
            int t = readBytes;
            readBytes = 0;
            return t;
        }

        @Override
        public int read() throws IOException {
            checkState();
            try {
                if (closedByRemote && available() == 0)
                    return -1;
                threadAccessRecorder.record();
                fillBuffer();
                final int readByte = buffer.getInputStream().read();
                readBytes += 1;
                return readByte;
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } catch (IOException e) {
                if (closedByRemote) {
                    // closed while trying to read
                    return -1;
                }
                throw e;
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    if (!closedByRemote)
                        throw new InterruptedIOException();
                }
            }
        }

        @Override
        public int available() throws IOException {
            return buffer.getInputStream().available();
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed)
                return;

            streamServiceManager.closeStream(this);
            closedByInternal();
            closed = true;
        }

        public void closedByInternal() {
            if (closed)
                return;

            this.closed = true;
            this.threadAccessRecorder.interrupt();

            if (remainingPackets != null) {
                for (StreamPacket p : remainingPackets) {
                    try {
                        p.reject();
                    } catch (IOException e) {
                        // connection broken, don't need to cancel rest
                        break;
                    }
                }
                remainingPackets.clear();
            }
        }

        @Override
        public boolean markSupported() {
            return buffer.getInputStream().markSupported();
        }

        @Override
        public int read(byte[] arg0) throws IOException {
            checkState();

            try {
                if (closedByRemote && available() == 0)
                    return -1;
                threadAccessRecorder.record();
                fillBuffer();
                final int read = buffer.getInputStream().read(arg0);
                readBytes += read;
                return read;
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } catch (IOException e) {
                if (closedByRemote) {
                    // closed while trying to read
                    return -1;
                }
                throw e;
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    if (!closedByRemote)
                        throw new InterruptedIOException();
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            buffer.getInputStream().mark(readlimit);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            checkState();

            try {
                if (closedByRemote && available() == 0)
                    return -1;
                threadAccessRecorder.record();
                fillBuffer();
                final int read = buffer.getInputStream().read(b, off, len);
                readBytes += read;
                return read;
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } catch (IOException e) {
                if (closedByRemote) {
                    // closed while trying to read
                    return -1;
                }
                throw e;
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    if (!closedByRemote)
                        throw new InterruptedIOException();
                }
            }
        }

        @Override
        public synchronized void reset() throws IOException {
            checkState();
            buffer.getInputStream().reset();
        }

        @Override
        public long skip(long n) throws IOException {
            checkState();

            try {
                threadAccessRecorder.record();
                fillBuffer();
                if (closedByRemote && available() == 0)
                    return -1;
                return buffer.getInputStream().skip(n);
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } finally {
                try {
                    threadAccessRecorder.release();
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }

        }

        protected void addPacket(StreamPacket packet) {
            if (isClosed()) {
                try {
                    packet.reject();
                } catch (IOException e) {
                    log.error("Could not reject packet: ", e);
                }
                return;
            }
            remainingPackets.add(packet);
            fillBuffer();
        }

        public StreamSession getSession() {
            return StreamSession.this;
        }

        public int streamID() {
            return streamID;
        }

        /**
         * Fills {@link #buffer} with data from {@link #remainingPackets} when
         * space is left
         */
        protected synchronized void fillBuffer() {
            if (remainingPackets.isEmpty())
                return;

            StreamPacket nextPacket = remainingPackets.peek();
            if (nextPacket == null)
                return;
            int remainingBuffer = buffer.getSpaceLeft();

            if (nextPacket.getSize() <= remainingBuffer) {
                remainingPackets.poll();
                try {
                    buffer.getOutputStream().write(nextPacket.getData());
                } catch (IOException e) {
                    log.error("Unexpected IOE: ", e);
                } catch (StreamException e) {
                    // ignore, we will be disposed :(
                }
            }
        }

        /**
         * Check state of stream and throw {@link IOException} when it is closed
         * local. When related {@link StreamSessionOutputStream} is closed, this
         * stream is closed when no more data is available.
         * 
         * @throws IOException
         *             when this stream itself is closed
         * @see #close()
         */
        protected void checkState() throws IOException {
            if (closed)
                throw new IOException("This stream is closed.");
            if (closedByRemote && remainingPackets.isEmpty()
                && available() == 0) {
                closed = true;
                threadAccessRecorder.interrupt();
            }
        }

        public boolean isClosed() {
            return closed || closedByRemote;
        }

    }

}
