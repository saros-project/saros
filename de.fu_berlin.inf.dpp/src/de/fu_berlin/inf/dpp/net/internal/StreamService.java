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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.exceptions.StreamServiceNotValidException;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionInputStream;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionOutputStream;

/**
 * Service defining stream-based sessions. Subclass it and add it to our
 * picoContainer (in {@link Saros#Saros()}) to work with it. A
 * {@link StreamService} is identified by it's name, which must be unique among
 * other defined {@link StreamService}s.
 * 
 * <dl>
 * <dt>initiator</dt>
 * <dd>User who starts the negotiation of the session</dd>
 * <dt>receiver</dt>
 * <dd>User acknowledging the negotiation; the end point of session's streams</dd>
 * </dl>
 * 
 * @author s-lau
 */
public abstract class StreamService {

    public static final int BUFFER_FACTOR = 10;

    /**
     * Called at receiver when session was accepted.
     */
    public abstract void startSession(StreamSession newSession);

    /**
     * buddy requests a session
     * 
     * @param from
     *            {@link User} to start a session with
     * @param initial
     *            service-specific object which is passed by the initiator
     * @return <code>true</code> for accepting, <code>false</code> for rejecting
     */
    public abstract boolean sessionRequest(User from, Object initial);

    /**
     * The name of the service. Should only contain letters <code>a-zA-Z</code>
     * and must be unique among other {@link StreamService}s.
     */
    public abstract String getServiceName();

    /**
     * Number of streams a session needs.
     * 
     * @return number of stream this session has. Should not be less than 1.
     */
    public int getStreamsPerSession() {
        return 1;
    }

    /**
     * Minimum length of data in bytes which will be transmitted at once. When
     * {@link #getMaximumDelay()} ms after arrival of data passed, smaller
     * chunks will be send. A chunk-size of zero implies no delay for the
     * stream.
     * 
     * @return chunksize's in bytes for each stream (array must be bigger than
     *         {@link #getStreamsPerSession()})
     */
    public int[] getChunkSize() {
        return new int[] { 1024 };
    }

    /**
     * Specifies how long smaller chunks of {@link #getChunkSize()} will be
     * locally cached before it is tried to send. A delay of zero implies no
     * chunk-size for the stream.
     * 
     * @return maximum delay in milliseconds for each stream (array must be
     *         bigger than {@link #getStreamsPerSession()})
     */
    public long[] getMaximumDelay() {
        return new long[] { 500 };
    }

    /**
     * <p>
     * Specifies for each stream the buffer size. This is the maximal number of
     * bytes which can be read or send at once. By default the buffer-size for
     * each stream is {@link #BUFFER_FACTOR}<code> * </code>
     * {@link #getChunkSize()}.
     * </p>
     * <p>
     * When buffers are full, the {@link StreamSessionOutputStream}
     * write-methods will block and the {@link StreamSessionInputStream} does
     * not accept packets until space is available. No data can be lost!
     * </p>
     * 
     * @return buffer sizes for each stream
     */
    public int[] getBufferSize() {
        int[] chunkSizes = getChunkSize();
        int[] bufferSizes = new int[chunkSizes.length];

        for (int i = 0; i < chunkSizes.length; i++) {
            bufferSizes[i] = BUFFER_FACTOR * chunkSizes[i];
        }

        return bufferSizes;
    }

    /**
     * Validates this service. Checked is:
     * <ul>
     * <li>{@link #getServiceName()} contains only letters <code>a-zA-Z</code></li>
     * <li>{@link #getStreamsPerSession()} is greater than zero</li>
     * <li>Streams characteristics ({@link #getChunkSize()},
     * {@link #getMaximumDelay()} and {@link #getBufferSize()}) are valid and
     * set for each stream</li>
     * </ul>
     * 
     * @throws StreamServiceNotValidException
     *             when service is not valid for some reason. Only the first
     *             detected error is thrown.
     */
    protected void validate() throws StreamServiceNotValidException {

        if (getServiceName() == null
            || !getServiceName().matches("\\A[a-zA-Z]+\\z"))
            throw new StreamServiceNotValidException(
                "Service-name is empty or contains invalid characters.", this);

        if (getStreamsPerSession() <= 0)
            throw new StreamServiceNotValidException(
                "A session can not have less than one stream.", this);

        if (getChunkSize().length < getStreamsPerSession())
            throw new StreamServiceNotValidException(
                "Chunk-size has to be set for every stream.", this);

        if (getMaximumDelay().length < getStreamsPerSession())
            throw new StreamServiceNotValidException(
                "Delays have to be set for every stream.", this);

        if (getBufferSize().length < getStreamsPerSession())
            throw new StreamServiceNotValidException(
                "Buffer-sizes have to be set for every stream.", this);

        for (int chunkSize : getChunkSize()) {
            if (chunkSize < 0)
                throw new StreamServiceNotValidException(
                    "Chunk-sizes have to be greater-than-or-equal 0.", this);
        }

        for (long delay : getMaximumDelay()) {
            if (delay < 0)
                throw new StreamServiceNotValidException(
                    "Delay has to be greater-than-or-equal 0.", this);
        }

        for (int bufferSize : getChunkSize()) {
            if (bufferSize <= 0)
                throw new StreamServiceNotValidException(
                    "Chunk-size has to be greater than 0.", this);
        }

    }

    @Override
    public String toString() {
        return (getServiceName() == null || getServiceName().length() == 0 ? "[no name set]"
            : getServiceName());
    }

}
