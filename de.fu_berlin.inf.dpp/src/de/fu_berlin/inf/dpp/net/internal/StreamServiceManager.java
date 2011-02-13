/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2010
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.Startable;
import org.picocontainer.annotations.Inject;

import com.google.common.collect.ImmutableList;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.ConnectionException;
import de.fu_berlin.inf.dpp.exceptions.ReceiverGoneException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.exceptions.StreamServiceNotValidException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager.StreamMetaPacketData.StreamClose;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.Stream;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionOutputStream;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * <p>
 * This {@link StreamServiceManager} allows the registration of defined
 * {@link StreamService}'s which use {@link InputStream} or {@link OutputStream}
 * instead packets to communicate. It handles session-negotiation, sending and
 * receiving data through {@link StreamSession} streams and stopping sessions.
 * </p>
 * <p>
 * Short introduction to a {@link StreamSession}'s life-cycle:
 * <ol>
 * <li>Subclass {@link StreamService} to define your own service</li>
 * <li>Add it to our picoContainer</li>
 * <li>Start a session with
 * {@link #createSession(StreamService, User, Serializable, StreamSessionListener)}
 * </li>
 * <li>You receive a valid session or an exception when it was not possible to
 * establish one</li>
 * <li>Send ({@link StreamSession#getOutputStream(int)}) or receive (
 * {@link StreamSession#getInputStream(int)}) data through session's streams</li>
 * <li>Stop session with {@link StreamSession#stopSession()}</li>
 * </ol>
 * </p>
 * 
 * @author s-lau
 */
@Component(module = "net")
public class StreamServiceManager implements Startable {

    public static Logger log = Logger.getLogger(StreamServiceManager.class);

    /**
     * Timeout in seconds for canceling negotiations
     */
    public static final int SESSION_NEGOTIATION_TIMEOUT = 60;

    protected DataTransferManager dataTransferManager;

    protected Saros saros;

    protected SarosSessionManager sessionManager;

    @Inject
    protected SessionIDObservable sarosSessionID;

    protected SarosSessionObservable sarosSessionObservable;

    protected IncomingTransferObjectExtensionProvider incomingTransferObjectExtensionProvider;

    protected volatile boolean started = false;

    /**
     * Registered services with their name as key.
     */
    protected Map<String, StreamService> registeredServices = new HashMap<String, StreamService>();

    /**
     * All established sessions.
     */
    protected Map<StreamPath, StreamSession> sessions = Collections
        .synchronizedMap(new HashMap<StreamPath, StreamSession>());

    /**
     * Running initiations which wait for {@link StreamMetaPacketData#ACCEPT} or
     * {@link StreamMetaPacketData#REJECT} from receiver.
     */
    protected Map<StreamPath, Initiation> initiations = Collections
        .synchronizedMap(new HashMap<StreamPath, Initiation>());

    protected volatile PacketSender sender;

    protected volatile PacketReceiver receiver;

    /**
     * SessionID for next session
     */
    private AtomicInteger nextStreamSessionID = new AtomicInteger(1);

    /**
     * The time in seconds a session can shut down before it is killed.
     */
    public static final int SESSION_SHUTDOWN_LIMIT = 30;

    /**
     * Executor for stopping sessions after {@link #SESSION_SHUTDOWN_LIMIT}
     */
    protected ScheduledExecutorService stopSessionExecutor;

    /**
     * Dispatches sessions to their services
     */
    protected ExecutorService sessionDispatcher;

    /**
     * Handles negotiation of session to the end-user
     */
    protected ExecutorService negotiatesToUser;

    /**
     * Handles negotiation via network to receiver of session
     */
    protected ExecutorService negotiations;

    /**
     * Counts the incoming packets. Only useful for tracing.
     */
    private long counter = 0;

    public StreamServiceManager(
        XMPPReceiver xmppReceiver,
        DataTransferManager dataTransferManager,
        SarosSessionObservable sarosSessionObservable,
        Saros saros,
        SarosSessionManager sessionManager,
        List<StreamService> streamServices,
        IncomingTransferObjectExtensionProvider incomingTransferObjectExtensionProvider) {

        this.dataTransferManager = dataTransferManager;
        this.sarosSessionObservable = sarosSessionObservable;
        this.saros = saros;
        this.sessionManager = sessionManager;
        this.incomingTransferObjectExtensionProvider = incomingTransferObjectExtensionProvider;

        // add all valid services
        StringBuilder addedServicesNames = new StringBuilder();
        addedServicesNames.append("StreamServices added:");
        for (StreamService streamService : streamServices) {
            try {
                streamService.validate();
            } catch (StreamServiceNotValidException e) {
                log.error("StreamService '" + e.invalidService
                    + "' is not valid, it will not be added.\nError is : ", e);
                continue;
            }

            if (!addService(streamService)) {
                log.warn("Service '" + streamService + "' already added!");
            } else {
                addedServicesNames.append("\n  ");
                addedServicesNames.append(streamService.getServiceName());
            }
        }
        log.debug(addedServicesNames.toString());

        registerListeners();

        xmppReceiver.addPacketListener(new StreamPacketListener(),
            new StreamPacketFilter());
    }

    protected void startThreads() {
        sender = new PacketSender();
        Utils.runSafeAsync("StreamServiceManagers-senderThread", log, sender);
        receiver = new PacketReceiver();
        Utils.runSafeAsync("StreamServiceManagers-receiverThread", log, receiver);
        stopSessionExecutor = Executors.newScheduledThreadPool(5,
            new NamedThreadFactory("StreamSessionStopper-"));

        sessionDispatcher = Executors
            .newSingleThreadExecutor(new NamedThreadFactory(
                "StreamSessionDispatcher-"));

        negotiatesToUser = Executors
            .newSingleThreadExecutor(new NamedThreadFactory(
                "StreamSessionNegotiationUser-"));

        negotiations = Executors.newFixedThreadPool(5, new NamedThreadFactory(
            "StreamSessionNegotiation-"));
    }

    public synchronized void start() {
        if (started)
            return;
        started = true;

        startThreads();
        counter = 0;
    }

    public synchronized void stop() {
        if (!started)
            return;
        started = false;

        stopSessionExecutor.shutdown();
        sessionDispatcher.shutdown();
        negotiatesToUser.shutdown();
        negotiations.shutdown();

        if (sender != null) {
            sender.dispose();
            sender = null;
        }
        if (receiver != null) {
            receiver.dispose();
            receiver = null;
        }

        synchronized (sessions) {
            // avoid ConcurrentModificationException when a sessions removes
            // itself
            for (StreamSession session : ImmutableList
                .copyOf(sessions.values())) {
                session.dispose();
            }
            sessions.clear();
        }

        for (Initiation initiation : initiations.values()) {
            initiation.cancel();
        }
        initiations.clear();

        stopSessionExecutor.shutdownNow();
        sessionDispatcher.shutdownNow();
        negotiatesToUser.shutdownNow();
        negotiations.shutdownNow();

        stopSessionExecutor = null;
        sessionDispatcher = null;
        negotiatesToUser = null;
        negotiations = null;
    }

    /**
     * Register {@link SharedProjectListener}, {@link ConnectionListener},
     * {@link SessionListener}.
     */
    protected void registerListeners() {
        final ISharedProjectListener sharedProjectListener = new SharedProjectListener();
        // re-add the listener when the session changes
        sarosSessionObservable
            .addAndNotify(new ValueChangeListener<ISarosSession>() {

                public void setValue(ISarosSession newValue) {
                    if (newValue != null)
                        newValue.addListener(sharedProjectListener);
                }

            });
        sessionManager.addSarosSessionListener(new SessionListener());
    }

    /**
     * Adds a service if it has not been added yet. Session-passing etc. will
     * take place in that passed instance.
     * 
     * @param streamService
     *            to add
     * @return <code>true</code> when service was added
     */
    protected boolean addService(StreamService streamService) {
        return registeredServices.put(streamService.getServiceName(),
            streamService) == null;
    }

    /**
     * Notifies data arrived on a stream.
     * 
     * @param out
     *            stream where data arrived
     * @param forceSend
     *            force sending no matter how many bytes (>0) are written
     * @param progress
     */
    protected void notifyDataAvailable(StreamSessionOutputStream out,
        boolean forceSend, final SubMonitor progress) {

        if (sender != null)
            sender.addNotification(sender.new DataNotification(out, progress,
                forceSend));
    }

    /**
     * Closes the given stream and marks at the sink/source at receiver that
     * other side is closed.
     * 
     * @param stream
     */
    protected void closeStream(Stream stream) {
        if (stream.isClosed())
            return;

        StreamSession session = stream.getSession();
        StreamClose closeDescription = new StreamClose(
            stream instanceof InputStream, stream.streamID());

        if (sender != null)
            sender.sendPacket(session.getTransferDescription(),
                StreamMetaPacketData.CLOSE.serializeInto(closeDescription),
                null);

    }

    /**
     * Given session will be terminated. A {@link StreamMetaPacketData#STOP} is
     * send to the receiver requesting him to stop. This method will notify
     * {@link StreamSessionListener#sessionStopped()} after packet was send.
     * 
     * @param session
     *            to terminate
     */
    protected void stopSession(final StreamSession session) {
        log.debug("stopping session " + session);

        if (session.shutdown != null) {
            log.warn("Session " + session + " is already being closed.");
            return;
        }

        // send stop packet
        TransferDescription transferDescription = session
            .getTransferDescription();

        if (sender != null)
            sender.sendPacket(transferDescription,
                StreamMetaPacketData.STOP.getIdentifier(), null);

        Runnable stopThread = Utils.wrapSafe(log, new SessionKiller(session));

        if (stopSessionExecutor != null) {
            stopSessionExecutor.schedule(stopThread, SESSION_SHUTDOWN_LIMIT,
                TimeUnit.SECONDS);
            session.shutdown = stopThread;
        } else {
            new Thread(stopThread).start();
        }

        if (session.sessionListener != null)
            session.sessionListener.sessionStopped();
    }

    /**
     * Given session finished to shut down after it was requested to stop
     * before. Signal it to other party with {@link StreamMetaPacketData#END}.
     * 
     * @param session
     *            which finished shutting down
     */
    protected void stoppedSession(StreamSession session) {
        if (session.stopped) {
            log.warn("Session " + session + " already stopped.");
            return;
        }
        log.info("Session " + session + " finished shutdown.");

        if (sender != null)
            sender.sendPacket(session.getTransferDescription(),
                StreamMetaPacketData.END.getIdentifier(), null);
    }

    /**
     * Convenient-method, create session with the default timeout
     * 
     * @threadsafe
     * @blocking
     * 
     * @param service
     *            service used for the session to be created
     * @param user
     *            start a session with
     * @param initiationDescription
     *            is passed to
     *            {@link StreamService#sessionRequest(User, Object)} at
     *            receiver's side, which is an optional description for the
     *            kind/purpose of this session. Can be <code>null</code>.
     * @param sessionListener
     *            Will be added to created session. Can be <code>null</code>.
     * @return A started session
     * @throws IllegalArgumentException
     *             when given {@link StreamService} is not found.
     * @throws TimeoutException
     *             Timeout reached, negotiation canceled.
     * @throws RemoteCancellationException
     *             Receiver rejected initiation-request.
     * @throws ExecutionException
     *             Unknown error happened during negotiation.
     * @throws InterruptedException
     *             Interrupted while negotiating.
     * @throws ConnectionException
     *             Not connected and can't send any data.
     * 
     * @see #SESSION_NEGOTIATION_TIMEOUT
     * @see #createSession(StreamService, User, Serializable,
     *      StreamSessionListener, int)
     */
    public StreamSession createSession(StreamService service, User user,
        Serializable initiationDescription,
        StreamSessionListener sessionListener) throws TimeoutException,
        RemoteCancellationException, ExecutionException, InterruptedException,
        ConnectionException {
        return createSession(service, user, initiationDescription,
            sessionListener, SESSION_NEGOTIATION_TIMEOUT);
    }

    /**
     * Try to establish a new session.
     * 
     * @threadsafe
     * @blocking
     * 
     * @param service
     *            service used for the session to be created
     * @param user
     *            start a session with
     * @param initiationDescription
     *            is passed to
     *            {@link StreamService#sessionRequest(User, Object)} at
     *            receiver's side, which is an optional description for the
     *            kind/purpose of this session. Can be <code>null</code>.
     * @param sessionListener
     *            Will be added to created session. Can be <code>null</code>.
     * @param timeout
     *            Cancel negotiation after given seconds.
     * @return A started session
     * @throws IllegalArgumentException
     *             when given {@link StreamService} is not found.
     * @throws TimeoutException
     *             Timeout reached, negotiation canceled.
     * @throws RemoteCancellationException
     *             Receiver rejected initiation-request.
     * @throws ExecutionException
     *             Unknown error happened during negotiation.
     * @throws InterruptedException
     *             Interrupted while negotiating.
     * @throws ConnectionException
     *             Not connected and can't send any data.
     */
    public StreamSession createSession(StreamService service, User user,
        Serializable initiationDescription,
        StreamSessionListener sessionListener, int timeout)
        throws TimeoutException, RemoteCancellationException,
        ExecutionException, InterruptedException, ConnectionException {
        if (!registeredServices.containsKey(service.getServiceName()))
            throw new IllegalArgumentException(
                "Tried to create a stream with unregistered service " + service);
        int initiationID = nextStreamSessionID.getAndIncrement();

        StreamPath streamPath = new StreamPath(saros.getMyJID(), service,
            initiationID);

        if (initiationDescription != null) {
            byte[] serializedInitial = Utils.serialize(initiationDescription);
            if (serializedInitial == null)
                log.warn("Given serializable is not serializable! "
                    + initiationDescription);
        }

        // holder for result
        Initiation initiation = new Initiation(service, initiationID,
            initiationDescription, streamPath, sessionListener);

        // store initiation
        initiations.put(streamPath, initiation);
        // send negotiation
        TransferDescription transferDescription = TransferDescription
            .createStreamMetaTransferDescription(user.getJID(),
                saros.getMyJID(), streamPath.toString(),
                sarosSessionID.getValue());

        if (sender != null)
            sender.sendPacket(transferDescription,
                StreamMetaPacketData.INIT.serializeInto(initiationDescription),
                SubMonitor.convert(new NullProgressMonitor()));
        else
            throw new ConnectionException();

        Future<StreamSession> initiationProcess = negotiations
            .submit(initiation);

        StreamSession session;
        try {
            session = initiationProcess.get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RemoteCancellationException) {
                RemoteCancellationException remoteCancellationException = (RemoteCancellationException) cause;
                throw remoteCancellationException;
            }
            log.error("Unknown error during negotiation: ", e.getCause());
            throw e;
        } finally {
            initiations.remove(streamPath);
        }

        return session;
    }

    /**
     * <p>
     * Represents a path to address packets to a particular
     * {@link StreamSession}.
     * </p>
     * <p>
     * The common representation is a {@link String} (used in
     * {@link TransferDescription#file_project_path}), which starts with the
     * type of packet (specified in {@link TransferDescription#type}) followed
     * by {@link #jid} (who initiated the session) and {@link #sessionID}.
     * </p>
     * <p>
     * Currently two types are used and implemented:
     * <ul>
     * <li> {@link TransferDescription.FileTransferType#STREAM_DATA}: A data
     * packet. Path additionally contains {@link #streamID} and {@link #size}.<br/>
     * Example:
     * 
     * <pre>
     * STREAM_DATA/alice1_fu@jabber.ccc.de/1/0/1048576
     * </pre>
     * 
     * </li>
     * <li> {@link TransferDescription.FileTransferType#STREAM_META}: A meta
     * packet. Path also stores the involved service name (to discover the
     * appropriate {@link StreamService} for an incoming initiation).
     * 
     * <pre>
     * STREAM_META/alice1_fu@jabber.ccc.de/1/SendFileSingle
     * </pre>
     * 
     * </li>
     * </ul>
     * All attributes are separated by {@link #PATH_DELIMITER}.
     * </p>
     * 
     */
    static class StreamPath {

        public static final char PATH_DELIMITER = '/';

        public String serviceName = null;
        public int size = 0;
        public int sessionID = 0;
        public int streamID = 0;
        /**
         * Is always the base JID, initiator of session
         */
        public String jid;
        public String type;

        /**
         * @throws IllegalArgumentException
         *             Given String can not be recognized as {@link StreamPath}
         */
        public StreamPath(String path) throws IllegalArgumentException {
            if (path == null)
                throw new IllegalArgumentException("Path can not be null");

            String[] tokens = path.split(String.valueOf(PATH_DELIMITER));

            this.type = tokens[0];
            if (this.type == null)
                throw new IllegalArgumentException("Type not known!");
            if (FileTransferType.STREAM_META.equals(this.type)) {
                if (tokens.length != 4)
                    throw new IllegalArgumentException(
                        "Unexpected number of tokens for a meta-path.");
                this.jid = tokens[1];
                this.serviceName = tokens[3];
                this.sessionID = Integer.valueOf(tokens[2]);
            } else if (FileTransferType.STREAM_DATA.equals(this.type)) {
                if (tokens.length != 5)
                    throw new IllegalArgumentException(
                        "Unexpected number of tokens for a data-path.");
                this.jid = tokens[1];
                this.sessionID = Integer.valueOf(tokens[2]);
                this.streamID = Integer.valueOf(tokens[3]);
                this.size = Integer.valueOf(tokens[4]);
            } else {
                throw new IllegalArgumentException("Type not valid!");
            }

        }

        /**
         * Builds a path for data packet.
         * 
         * @param jid
         *            initiator of session
         * @param sessionID
         *            of {@link StreamSession}
         * @param streamID
         *            to which stream in {@link StreamSession} this packet
         *            belongs
         * @param size
         *            of data in bytes
         */
        public StreamPath(JID jid, int sessionID, int streamID, int size) {
            this.type = FileTransferType.STREAM_DATA;
            this.jid = jid.getBase();
            this.size = size;
            this.sessionID = sessionID;
            this.streamID = streamID;
        }

        /**
         * Builds a path for meta packet.
         * 
         * @param jid
         *            initiator of session
         * @param service
         *            on which session is based
         * @param sessionID
         *            of {@link StreamSession} or it's initiation
         */
        public StreamPath(JID jid, StreamService service, int sessionID) {
            this.type = FileTransferType.STREAM_META;
            this.jid = jid.getBase();
            this.serviceName = service.getServiceName();
            this.sessionID = sessionID;

        }

        public JID getInitiator() {
            return new JID(jid);
        }

        /**
         * @return representation for this path as {@link String}
         */
        @Override
        public String toString() {
            if (FileTransferType.STREAM_DATA.equals(type)) {
                return String.format("%6$s%5$c%1$s%5$c%2$d%5$c%3$d%5$c%4$d",
                    jid, sessionID, streamID, size, PATH_DELIMITER, type);
            } else if (FileTransferType.STREAM_META.equals(type)) {
                return String.format("%5$s%4$c%1$s%4$c%3$d%4$c%2$s", jid,
                    serviceName, sessionID, PATH_DELIMITER, type);
            } else {
                throw new RuntimeException("Unknown type!");
            }
        }

        /**
         * hashCode is based on {@link #jid} and {@link #sessionID}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((jid == null) ? 0 : jid.hashCode());
            result = prime * result + sessionID;
            return result;
        }

        /**
         * Two StreamPath's are equal when they represent the same session, when
         * {@link #sessionID} and {@link #jid} are equal.
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StreamPath other = (StreamPath) obj;
            if (jid == null) {
                if (other.jid != null)
                    return false;
            } else if (!jid.equals(other.jid))
                return false;
            if (sessionID != other.sessionID)
                return false;
            return true;
        }

        protected static String createDataPath(StreamSession session,
            int streamID, int size) {
            return new StreamPath(session.initiator, session.sessionID,
                streamID, size).toString();
        }

        protected static String createMetaPath(StreamSession session) {
            return new StreamPath(session.initiator, session.getService(),
                session.sessionID).toString();
        }
    }

    /**
     * <p>
     * MetaPackets are session-life-cycle related packets of type
     * {@link FileTransferType#STREAM_META}. These {@link Enum}s contain the
     * data to identify the type of {@link StreamMetaPacketData}. Additionally
     * {@link Serializable}'s can be merged into data.
     * </p>
     * <p>
     * The format of a data-packet is one byte identifying the type (see
     * Enum-constants). When this packet should contain serialized data, it is
     * followed by a delimiter (null-byte) and the serialized object.
     * </p>
     * 
     * TODO serialize data with protobuf or xstream
     */
    static enum StreamMetaPacketData {
        /**
         * Initiate/request a session: expected to receive answer
         * {@link StreamMetaPacketData#ACCEPT} or
         * {@link StreamMetaPacketData#REJECT}. Can carry an initiation-object.
         */
        INIT(0x01),
        /**
         * Reject a session-request. Session will be closed.
         */
        REJECT(0x02),
        /**
         * Accepts a session-request. Session will be valid.
         */
        ACCEPT(0x03),
        /**
         * Request to stop a session. Expected to receive
         * {@link StreamMetaPacketData#END} when stopped.
         */
        STOP(0x04),
        /**
         * Confirm that session is ready to be killed.
         */
        END(0x05),
        /**
         * Closes a stream in a session. Carries {@link StreamClose} to describe
         * which stream is closed.
         */
        CLOSE(0x06);

        /**
         * Byte representing type of this packet.
         */
        byte identifier;

        protected static Map<Byte, StreamMetaPacketData> packets = new HashMap<Byte, StreamMetaPacketData>();

        static {
            for (StreamMetaPacketData m : StreamMetaPacketData.values())
                packets.put(m.identifier, m);
        }

        /**
         * 
         * @param identifier
         *            uses only lowest 8 bits as identifier
         */
        StreamMetaPacketData(int identifier) {
            this.identifier = (byte) identifier;
        }

        protected byte[] getIdentifier() {
            return new byte[] { this.identifier };
        }

        /**
         * Extracts an object from a {@link StreamMetaPacketData}'s data.
         * 
         * @param data
         * @return deserialized object or <code>null</code>
         */
        protected static Object deserializeFrom(byte[] data) {
            // at least one additional byte (which can be serialized data)
            if (data.length < 3)
                return null;
            // no MetaPacket (no identifier or delimiter)
            if (getPacket(data) == null || data[1] != 0)
                return null;

            byte[] serialized = new byte[data.length - 2];
            System.arraycopy(data, 2, serialized, 0, serialized.length);

            return Utils.deserialize(serialized);
        }

        /**
         * Merges a serialized {@link Object} into {@link StreamMetaPacketData}
         * 's data.
         * 
         * @param o
         * @return
         */
        protected byte[] serializeInto(Serializable o) {
            byte[] serialized = Utils.serialize(o);
            if (serialized == null)
                return new byte[] { this.identifier };

            byte[] result = new byte[serialized.length + 2];

            result[0] = this.identifier;
            result[1] = 0;
            System.arraycopy(serialized, 0, result, 2, serialized.length);

            return result;
        }

        protected static StreamMetaPacketData getPacket(byte[] data) {
            return data.length > 0 ? packets.get(data[0]) : null;
        }

        /**
         * Description for a stream which should be closed. After a local stream
         * has been closed, this is send to receiver that he can close his
         * stream.
         */
        static class StreamClose implements Serializable {
            private static final long serialVersionUID = -5326543581762056077L;
            /**
             * Was this stream an {@link InputStream} (at sender of this
             * object)? Then we should close the local output.
             */
            boolean senderInputstreamClosed;
            int streamID;

            public StreamClose(boolean senderInputstreamClosed, int streamID) {
                super();
                this.senderInputstreamClosed = senderInputstreamClosed;
                this.streamID = streamID;
            }

        }
    }

    /**
     * Holds the initiation (we send {@link StreamMetaPacketData#INIT}, wait for
     * response) of a session.
     */
    static class Initiation implements Callable<StreamSession> {

        protected StreamPath streamPath;
        protected StreamService service;
        protected StreamSessionListener sessionListener;
        protected int initiationID;
        protected Thread initiationThread = null;
        protected StreamSession session = null;
        /**
         * Blocks the call of {@link #call()} until attempt to negotiate a
         * session is rejected or accepted.
         * 
         * @see #startSession(StreamSession)
         * @see #rejectSession()
         */
        protected CountDownLatch responseLock = new CountDownLatch(1);
        protected Boolean rejected = null;
        protected Object initial;
        protected boolean cancelled = false;

        public Initiation(StreamService service, int initiationID,
            Object initial, StreamPath streamPath,
            StreamSessionListener sessionListener) {
            this.service = service;
            this.initiationID = initiationID;
            this.initial = initial;
            this.streamPath = streamPath;
            this.sessionListener = sessionListener;
        }

        /**
         * Blocks until {@link #rejectSession()} or
         * {@link #startSession(StreamSession)} are called.
         * 
         * @throws RemoteCancellationException
         *             Receiver rejected session
         * @throws InterruptedException
         *             Interrupted while waiting for result. This means
         *             {@link StreamServiceManager} is shutting down.
         */
        public StreamSession call() throws Exception {
            if (cancelled)
                throw new InterruptedException("Initiation was cancelled");
            initiationThread = Thread.currentThread();
            responseLock.await();
            assert rejected != null;
            if (rejected)
                throw new RemoteCancellationException(
                    "Recipient rejected session for " + service);
            assert session != null;
            session.setListener(sessionListener);
            return session;
        }

        /**
         * Let {@link #call()} return given {@link StreamSession}
         * 
         * @param session
         */
        protected synchronized void startSession(StreamSession session) {
            this.session = session;
            notifyWaiting(false);
        }

        /**
         * Let {@link #call()} throw a {@link SarosCancellationException}
         */
        protected synchronized void rejectSession() {
            notifyWaiting(true);

        }

        /**
         * Sets the rejected-status and releases lock in {@link #call()}
         * 
         * @param reject
         */
        private void notifyWaiting(boolean reject) {
            rejected = reject;
            responseLock.countDown();
        }

        /**
         * Cancels this initiation by interrupting this Thread if already
         * started.
         */
        public void cancel() {
            if (initiationThread != null)
                initiationThread.interrupt();
            cancelled = true;
        }

    }

    /**
     * <p>
     * Send data sequentially to {@link DataTransferManager}. A session's
     * {@link StreamSessionOutputStream} notifies when data was written, then
     * this running {@link Thread} will poll the stream for it's data after
     * processing earlier notifications.
     * </p>
     * <p>
     * Another purpose is sending data immediately via
     * {@link PacketSender#sendPacket(TransferDescription, byte[], SubMonitor)}.
     * </p>
     * <p>
     * Interrupting the Thread will cause a shutdown.
     * </p>
     * 
     * TODO handle SubMonitors that session's could use them
     */
    class PacketSender implements Runnable {

        protected BlockingQueue<DataNotification> notifications = new LinkedBlockingQueue<DataNotification>();

        /**
         * For these sessions no data should be send anymore
         */
        protected Set<StreamSession> blockedSessions = new HashSet<StreamSession>();

        protected Thread senderThread;

        protected volatile boolean disposed = false;

        /**
         * Packet which is send now
         */
        protected StreamPacket currentPacket = null;

        public void run() {
            senderThread = Thread.currentThread();
            while (true) {
                if (Thread.interrupted())
                    return;
                DataNotification notification = null;
                StreamPacket packetToSend = null;
                try {
                    notification = notifications.take();

                    synchronized (notifications) {
                        // only process notification when removeData(...) is not
                        // running
                        packetToSend = notification.getPacket();

                        if (packetToSend == null
                            || blockedSessions.contains(packetToSend
                                .getSession())) {
                            continue;
                        }
                    }
                    internalSend(packetToSend);
                } catch (InterruptedException e) {
                    // shutdown
                    return;
                } finally {
                    /*
                     * TODO .done() wrong when skipped notification: stream
                     * contained less than minimal-chunk-size and force send is
                     * false -> cache monitor until data is send
                     */

                    if (packetToSend != null && notification != null
                        && notification.progress != null)
                        notification.progress.done();
                }

            }
        }

        /**
         * sends immediately to dtm
         */
        private synchronized void internalSend(StreamPacket packet) {
            try {
                currentPacket = packet;
                dataTransferManager.sendData(packet.getTransferDescription(),
                    packet.data, packet.progress);

            } catch (IOException e) {
                log.error("Connection broken: ", e);
                if (packet.getSession() != null) {
                    packet.getSession().reportErrorAndDispose(
                        new ConnectionException(e));
                    removeData(packet.getSession());
                }
            } catch (SarosCancellationException e) {
                /*
                 * ignore: user gone (will be reported by SharedProjectListener)
                 * or stream closed (drop data silently)
                 */
            }
        }

        /**
         * Sends immediately a packet to {@link DataTransferManager} when it's
         * not related to a established session. Otherwise it will be queued.
         * 
         * @param packet
         */
        protected void sendPacket(StreamPacket packet) {
            if (packet.getSession() == null)
                internalSend(packet);
            else
                notifications.add(new DataNotification(packet));

        }

        /**
         * Convenience method
         * 
         * @see #sendPacket(StreamPacket)
         */
        protected void sendPacket(TransferDescription transferDescription,
            byte[] data, SubMonitor progress) {
            try {
                sendPacket(new StreamPacket(transferDescription, data, progress));
            } catch (IllegalArgumentException e) {
                // packet invalid
                return;
            }
        }

        /**
         * Adds a {@link DataNotification} which will be processed later.
         * 
         * @param notification
         */
        protected void addNotification(DataNotification notification) {
            synchronized (notifications) {
                notifications.add(notification);
            }
        }

        /**
         * Removes all notifications and data for given session from queue,
         * aborts sending when data is send now and prevents that future data is
         * send.
         * 
         * @param session
         *            for which no data should be send anymore
         */
        protected void removeData(StreamSession session) {
            assert session != null;
            if (blockedSessions.contains(session))
                return;

            synchronized (notifications) {
                blockedSessions.add(session);
                if (currentPacket != null
                    && session.getStreamPath().equals(currentPacket.streamPath))
                    currentPacket.progress.setCanceled(true);
                for (DataNotification notification : notifications) {
                    StreamPath streamPath = notification.getStreamPath();
                    if (streamPath.equals(session.getStreamPath()))
                        notifications.remove(notification);
                }
            }
        }

        protected void dispose() {
            if (disposed)
                return;
            disposed = true;

            senderThread.interrupt();

            synchronized (notifications) {
                for (DataNotification dn : notifications) {
                    if (dn.progress != null)
                        dn.progress.setCanceled(true);
                }
                notifications.clear();
            }

        }

        /**
         * This type of notification has two purposes
         * <ol>
         * <li>Notify some data in a stream</li>
         * <li>Notify a packet to be send</li>
         * </ol>
         */
        protected class DataNotification {
            StreamSessionOutputStream stream;
            SubMonitor progress;
            boolean removeAllAvailableData = false;
            StreamPacket packet;

            public DataNotification(StreamSessionOutputStream stream,
                SubMonitor progress, boolean removeAllAvailableData) {
                super();
                this.stream = stream;
                this.progress = progress;
                this.removeAllAvailableData = removeAllAvailableData;
            }

            public DataNotification(StreamPacket packet) {
                this.packet = packet;
            }

            protected StreamPath getStreamPath() {
                return packet == null ? stream.getSession().getStreamPath()
                    : packet.getStreamPath();
            }

            /**
             * 
             * @return packet which should be send or <code>null</code> when
             *         nothing to send
             */
            protected StreamPacket getPacket() {
                if (stream != null) {
                    StreamSession session = stream.getSession();
                    if (session.disposed || session.receiverStopped
                        || session.stopped)
                        return null;

                    byte[] data = stream.getData(removeAllAvailableData);

                    if (data == null
                        || (progress != null && progress.isCanceled()))
                        return null;

                    TransferDescription transferDescription = TransferDescription
                        .createStreamDataTransferDescription(
                            stream.getSession().remoteJID, saros.getMyJID(),
                            sarosSessionID.getValue(),
                            stream.getStreamPath(data.length).toString());

                    try {
                        return new StreamPacket(transferDescription, data,
                            progress);
                    } catch (IllegalArgumentException e) {
                        // packet invalid
                        return null;
                    }
                } else {
                    assert packet != null;
                    return packet;
                }
            }
        }
    }

    /**
     * <p>
     * This class receives all {@link IncomingTransferObject}'s from
     * {@link DataTransferManager} and processes
     * {@link FileTransferType#STREAM_META} and
     * {@link FileTransferType#STREAM_DATA}.
     * </p>
     * <p>
     * Interrupting the Thread will cause a shutdown.
     * </p>
     */
    class PacketReceiver implements Runnable {

        protected BlockingQueue<StreamPacket> incomingPackets = new LinkedBlockingQueue<StreamPacket>();

        protected Thread receiverThread;

        protected volatile boolean disposed = false;

        public void run() {
            receiverThread = Thread.currentThread();
            while (true) {
                StreamPacket packet;
                try {
                    packet = incomingPackets.take();
                } catch (InterruptedException e) {
                    return;
                }

                processPacket(packet);

                if (Thread.interrupted()) {
                    return;
                }
            }
        }

        protected synchronized void dispose() {
            if (disposed)
                return;
            disposed = true;

            receiverThread.interrupt();
            for (StreamPacket p : incomingPackets) {
                try {
                    p.reject();
                } catch (IOException e1) {
                    break;
                }
            }
            incomingPackets.clear();
        }

        /**
         * Adds an incoming packet to our working queue to process it later.
         * 
         * @param packet
         */
        protected void offerPacket(StreamPacket packet) {
            if (disposed) {
                try {
                    packet.reject();
                } catch (IOException e) {
                    // ignore
                }
                return;
            }

            incomingPackets.add(packet);
        }

        /**
         * Process an incoming {@link StreamPacket}.
         * {@link FileTransferType#STREAM_DATA}-packets are passed to session,
         * {@link FileTransferType#STREAM_META}-packets will be processed by
         * {@link #processMeta(StreamPacket)}
         * 
         * @param packet
         */
        protected void processPacket(StreamPacket packet) {
            counter++;
            TransferDescription description = packet.getTransferDescription();

            log.trace("Packet " + counter + " arrived");
            if (FileTransferType.STREAM_DATA.equals(description.type)) {
                StreamSession session = sessions.get(packet.getStreamPath());
                if (session == null) {
                    log.error("Received packet for an unknown session. Path is "
                        + packet.getStreamPath());
                    try {
                        packet.reject();
                    } catch (IOException e) {
                        log.warn("Could not reject unknown data packet: ", e);
                    }
                    return;
                }
                session.addPacket(packet);
            } else if (FileTransferType.STREAM_META.equals(description.type)) {
                processMeta(packet);
            } else {
                log.error("Received unknown packet type: " + description.type);
            }

        }

        /**
         * Process incoming {@link StreamMetaPacketData}'s
         */
        protected void processMeta(StreamPacket packet) {
            byte[] data;

            try {
                data = packet.getData();
            } catch (StreamException e) {
                log.warn("Could not open packet: ", e);
                // stop processing
                return;
            }

            final TransferDescription transferDescription = packet
                .getTransferDescription();
            // get type of packet
            StreamMetaPacketData metaPacket = StreamMetaPacketData
                .getPacket(data);
            if (metaPacket == null) {
                log.error("Received unknown meta packet: " + new String(data));
                return;
            }

            final Initiation initiation;
            final StreamPath streamPath;
            try {
                streamPath = new StreamPath(
                    transferDescription.file_project_path);
            } catch (IllegalArgumentException e) {
                log.error("Packet had invalid stream-path: "
                    + (transferDescription.file_project_path == null ? "none"
                        : transferDescription.file_project_path));
                return;
            }
            final StreamSession session = sessions.get(streamPath);

            log.debug("Received " + metaPacket.name() + " for session "
                + streamPath);
            switch (metaPacket) {
            case INIT:
                // remote peer wants to create a session

                // validate service
                final StreamService service = registeredServices
                    .get(streamPath.serviceName);
                if (service == null) {
                    log.error("Received inititation request for unknown service: "
                        + streamPath.serviceName);
                    return;
                }

                final Object initiationDescription = StreamMetaPacketData
                    .deserializeFrom(data);

                if (sessions.containsKey(streamPath)) {
                    log.error("Received initiation packet more than once from "
                        + transferDescription.sender);
                    return;
                }

                ISarosSession sarosSession = sarosSessionObservable.getValue();
                if (sarosSession == null) {
                    log.warn("Not in a shared project, discarding packet!");
                    return;
                }

                final User from = sarosSession
                    .getUser(transferDescription.sender);
                if (from == null) {
                    log.warn("Buddy left, discarding packet!");
                    return;
                }

                /*
                 * Ask service for accept and send decision to client. When
                 * accepted, a new session will be created.
                 */
                negotiatesToUser.execute(Utils.wrapSafe(log, new Runnable() {
                    public void run() {
                        log.debug("Starting session request to service");

                        boolean startSession = false;
                        try {
                            startSession = service.sessionRequest(from,
                                initiationDescription);
                        } catch (Exception e) {
                            log.error("Service crashed: ", e);
                        }

                        if (startSession) {
                            log.debug("Service accepted, try to create session");

                            final StreamSession newSession;
                            synchronized (sessions) {
                                if (sessions.containsKey(streamPath)) {
                                    log.warn("Session already created, received INIT twice: "
                                        + streamPath);
                                    return;
                                }
                                newSession = new StreamSession(
                                    StreamServiceManager.this, service,
                                    transferDescription.sender,
                                    transferDescription.sender,
                                    streamPath.sessionID, initiationDescription);

                            }

                            if (sender != null) {
                                sender.sendPacket(
                                    newSession.getTransferDescription(),
                                    StreamMetaPacketData.ACCEPT.getIdentifier(),
                                    null);
                                log.debug("Accept-packet send.");
                                sessions.put(streamPath, newSession);
                            } else
                                // can not create, not connected
                                newSession.dispose();

                            sessionDispatcher.execute(Utils.wrapSafe(log,
                                new Runnable() {
                                    public void run() {
                                        service.startSession(newSession);
                                        log.debug("Session started");
                                    }
                                }));
                        } else {
                            log.debug("Session rejected, will send reject-packet.");

                            if (sender != null) {
                                sender.sendPacket(
                                    TransferDescription
                                        .createStreamMetaTransferDescription(
                                            transferDescription.sender,
                                            transferDescription.recipient,
                                            streamPath.toString(),
                                            sarosSessionID.getValue()),
                                    StreamMetaPacketData.REJECT.getIdentifier(),
                                    null);

                                log.debug("Reject-packet send.");
                            }
                        }

                    }
                }));

                break;
            case STOP:
                // remote peer wants to terminate this session

                if (session == null) {
                    log.error("Received STOP-packet for unknown session "
                        + streamPath);
                    return;
                }
                log.debug("Received STOP for session " + session);

                if (session.shutdown != null) {
                    log.error("Session " + session + " already stopped.");
                    return;
                }
                Runnable stopThread = Utils.wrapSafe(log, new SessionKiller(
                    session));
                session.shutdown = stopThread;

                stopSessionExecutor.schedule(stopThread,
                    SESSION_SHUTDOWN_LIMIT, TimeUnit.SECONDS);

                if (session.sessionListener != null)
                    session.sessionListener.sessionStopped();

                break;
            case END:
                // remote peer finished shutdown and disposed his session

                if (session == null) {
                    log.warn("Received END for an unknown session:"
                        + streamPath);
                    return;
                }

                if (session.receiverStopped) {
                    log.warn("Receiver stopped already, received another STOPPED "
                        + session);
                    return;
                }

                session.receiverStopped = true;

                if (sender != null)
                    sender.removeData(session);

                break;
            case REJECT: //$FALL-THROUGH$
            case ACCEPT:
                initiation = initiations.remove(streamPath);

                if (initiation == null) {
                    log.warn("Received REJECT/ACCEPT packet I have no initiation for!");
                    return;
                }
                if (metaPacket == StreamMetaPacketData.REJECT)
                    initiation.rejectSession();
                else {
                    final StreamSession newSession = new StreamSession(
                        StreamServiceManager.this, initiation.service,
                        transferDescription.sender, saros.getMyJID(),
                        streamPath.sessionID, initiation.initial);
                    sessions.put(streamPath, newSession);

                    initiation.startSession(newSession);
                    log.debug("Session started, passed to initiation.");
                }
                break;
            case CLOSE:
                // a stream in a session closed

                if (session == null) {
                    log.warn("Received CLOSE for a session which not exists");
                    return;
                }

                Object o = StreamMetaPacketData.deserializeFrom(data);
                StreamClose closeDesc;
                if (o instanceof StreamClose) {
                    closeDesc = (StreamClose) o;
                } else {
                    log.error("Received unknown object in CLOSE-packet");
                    return;
                }

                Stream toClose = (Stream) (closeDesc.senderInputstreamClosed ? session
                    .getOutputStream(closeDesc.streamID) : session
                    .getInputStream(closeDesc.streamID));

                toClose.closedByRemote();

                break;
            default:
                log.error("Please implement case for this unknown metapacket with identifier "
                    + Byte.valueOf(data[0]));

            }

        }
    }

    /**
     * This {@link Runnable} invalidates/disposes a session.
     */
    class SessionKiller implements Runnable {

        private StreamSession session;

        public SessionKiller(StreamSession session) {
            super();
            this.session = session;
        }

        public void run() {
            log.debug("Killing session " + session);
            StreamPath streamPath = session.getStreamPath();
            if (sessions.get(streamPath) == null) {
                log.warn("Session " + session + " already closed");
                return;
            }
            if (!(session.receiverStopped && session.stopped)) {
                String bad;
                if (session.receiverStopped || session.stopped)
                    bad = session.stopped ? "receiver" : "us";
                else
                    bad = "both";
                log.warn("Session not properly shut downed by " + bad
                    + ". Will kill session " + session);
            }
            session.dispose();
            sessions.remove(streamPath);
        }
    }

    /**
     * Abstraction for incoming and outgoing data-packets.
     */
    public class StreamPacket {
        /**
         * Timeout for receiving data (
         * {@link IncomingTransferObject#accept(SubMonitor)}) in seconds.
         */
        public static final int DATA_RECEIVE_TIMEOUT = 10;

        protected TransferDescription transferDescription = null;
        protected IncomingTransferObject ito = null;
        protected byte[] data = null;
        protected StreamPath streamPath;
        protected SubMonitor progress = null;

        /**
         * Incoming packet.
         * 
         * @param ito
         * @throws IllegalArgumentException
         *             Transfer-object contains no valid {@link StreamPath}
         */
        public StreamPacket(IncomingTransferObject ito)
            throws IllegalArgumentException {
            this.ito = ito;
            this.transferDescription = ito.getTransferDescription();
            this.streamPath = new StreamPath(
                ito.getTransferDescription().file_project_path);
        }

        /**
         * Outgoing packet.
         * 
         * @param desc
         * @param data
         * @throws IllegalArgumentException
         *             When descriptions file-path is not a {@link StreamPath}
         */
        public StreamPacket(TransferDescription desc, byte[] data,
            SubMonitor progress) throws IllegalArgumentException {
            this.transferDescription = desc;
            this.data = data;
            this.progress = progress == null ? SubMonitor
                .convert(new NullProgressMonitor()) : progress;
            this.streamPath = new StreamPath(desc.file_project_path);
        }

        /**
         * Reject this packet (when incoming).
         * 
         * @throws IOException
         * @see IncomingTransferObject#reject()
         */
        public void reject() throws IOException {
            if (ito != null)
                ito.reject();
        }

        /**
         * Returns the data this packet contains. When this packet is incoming,
         * the first call blocks, then cached. When an error occurs and can not
         * receive any data, it's is reported to packets session, if there is
         * one, and {@link StreamException} will be thrown.
         * 
         * @blocking
         * @caching
         * @throws StreamException
         *             when any error occurs and no data can be retrieved
         */
        public byte[] getData(final SubMonitor progress) throws StreamException {
            if (this.data != null)
                return this.data;

            try {
                data = ito.accept(progress == null ? SubMonitor.convert(null)
                    : progress);

            } catch (SarosCancellationException cancellation) {
                log.error("Receiver cancelled unexpected: ", cancellation);
                if (this.getSession() != null)
                    this.getSession().reportErrorAndDispose(
                        new ReceiverGoneException(cancellation));
            } catch (IOException ioe) {
                log.error("Connection broken: ", ioe);
                if (this.getSession() != null)
                    this.getSession().reportErrorAndDispose(
                        new ConnectionException(ioe));

            }

            if (data == null || data.length == 0) {
                // received none -> error
                throw new StreamException("Packet contained no data");
            }

            if (transferDescription.type.equals(FileTransferType.STREAM_DATA)
                && data.length != streamPath.size) {
                if (streamPath.size > data.length) {
                    log.error("Lost bytes! Got " + data.length + ", expected "
                        + streamPath.size);
                    throw new StreamException(
                        "Received less data than announced.");
                }
                byte[] new_data = new byte[streamPath.size];
                System.arraycopy(data, 0, new_data, 0, streamPath.size);
                data = new_data;
            }
            return this.data;
        }

        /**
         * Returns the data this packet contains. When this packet is incoming,
         * the first call blocks, then cached. When an error occurs and can not
         * receive any data, it's is reported to packets session, if there is
         * one, and {@link StreamException} will be thrown.
         * 
         * @blocking
         * @caching
         * @throws StreamException
         *             when any error occurs and no data can be retrieved
         * 
         * @see #getData(SubMonitor)
         */
        public byte[] getData() throws StreamException {
            return getData(null);
        }

        /**
         * @return number of bytes in packets data
         */
        public int getSize() {
            return streamPath.size;
        }

        public StreamPath getStreamPath() {
            return this.streamPath;
        }

        /**
         * @return session for/of this packet
         */
        public StreamSession getSession() {
            return sessions.get(streamPath);
        }

        public TransferDescription getTransferDescription() {
            return transferDescription;
        }

        /**
         * @return <code>true</code> when we received this packet
         */
        public boolean isIncoming() {
            return ito != null;
        }

    }

    class StreamPacketListener implements PacketListener {

        public void processPacket(Packet packet) {
            IncomingTransferObject ito = incomingTransferObjectExtensionProvider
                .getPayload(packet);
            try {
                if (receiver != null)
                    receiver.offerPacket(new StreamPacket(ito));
            } catch (IllegalArgumentException e) {
                log.error("Received not valid packet: " + ito);
                return;
            }
        }
    }

    class StreamPacketFilter implements PacketFilter {

        public boolean accept(Packet packet) {
            IncomingTransferObject payload = incomingTransferObjectExtensionProvider
                .getPayload(packet);

            if (payload == null) {
                return false;
            }

            TransferDescription transferDescription = payload
                .getTransferDescription();
            if (!Utils.equals(transferDescription.sessionID,
                sarosSessionID.getValue()))
                return false;

            return ObjectUtils.equals(transferDescription.type,
                FileTransferType.STREAM_DATA)
                || ObjectUtils.equals(transferDescription.type,
                    FileTransferType.STREAM_META);
        }
    }

    /**
     * Removes a user's sessions when he leaves the shared project
     */
    protected final class SharedProjectListener implements
        ISharedProjectListener {
        public void userLeft(User user) {
            // remove his sessions
            synchronized (sessions) {
                // avoid ConcurrentModificationException when a sessions removes
                // itself
                for (StreamSession session : ImmutableList.copyOf(sessions
                    .values())) {
                    if (session.remoteJID.equals(user.getJID())) {
                        session
                            .reportErrorAndDispose(new ReceiverGoneException());
                    }
                }
            }
        }

        public void userJoined(User user) {
            // NOP
        }

        public void permissionChanged(User user) {
            // NOP
        }

        public void invitationCompleted(User user) {
            // NOP
        }

        public void projectAdded(IProject project) {
            // NOP

        }
    }

    /**
     * Resets the {@link StreamServiceManager} when saros' session stopped.
     */
    protected final class SessionListener extends AbstractSarosSessionListener {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            StreamServiceManager.this.start();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            StreamServiceManager.this.stop();
        }

    }

}
