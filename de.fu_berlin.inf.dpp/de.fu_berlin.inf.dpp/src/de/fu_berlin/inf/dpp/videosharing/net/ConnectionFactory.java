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
package de.fu_berlin.inf.dpp.videosharing.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.picocontainer.Disposable;

import com.Ostermiller.util.CircularByteBuffer;

import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Mode;

/**
 * Factory for creating the needed connections for video-sharing.
 * 
 * @author s-lau
 * 
 */
public class ConnectionFactory implements Disposable {

    protected Mode mode;

    protected StreamSession session;
    protected OutputStream hostVideoOut = null;
    protected ObjectInputStream hostActivitiesIn = null;
    protected ObjectInputStream hostStatisticsIn = null;
    protected InputStream clientVideoIn = null;
    protected ObjectOutputStream clientActivitiesOut = null;
    protected ObjectOutputStream clientStatisticsOut = null;
    protected ObjectOutputStream hostErrorOut = null;
    protected ObjectInputStream clientErrorIn = null;
    protected static final int LOCAL_VIDEO_BUFFER = 4 * 1024 * 1024;

    protected CircularByteBuffer videoCirc;
    protected CircularByteBuffer activityCirc;
    protected CircularByteBuffer staticticsCirc;
    protected CircularByteBuffer errorCirc;

    /**
     * 
     * @param session
     * @param mode
     * @throws IOException
     */
    public ConnectionFactory(StreamSession session, Mode mode)
        throws IOException {
        this.mode = mode;
        this.session = session;

        try {
            switch (mode) {
            case CLIENT:
                clientVideoIn = session.getInputStream(0);

                clientStatisticsOut = new ObjectOutputStream(session
                    .getOutputStream(0));
                clientStatisticsOut.flush();
                clientActivitiesOut = new ObjectOutputStream(session
                    .getOutputStream(1));
                clientActivitiesOut.flush();
                clientErrorIn = new ObjectInputStream(session.getInputStream(1));

                break;
            case HOST:
                hostVideoOut = session.getOutputStream(0);

                hostStatisticsIn = new ObjectInputStream(session
                    .getInputStream(0));
                hostActivitiesIn = new ObjectInputStream(session
                    .getInputStream(1));
                hostErrorOut = new ObjectOutputStream(session
                    .getOutputStream(1));
                hostErrorOut.flush();
                break;
            default:
                break;
            }
        } catch (IOException e) {
            session.dispose();
            throw e;
        }

    }

    /**
     * Create local connections.
     */
    public ConnectionFactory() {
        this.mode = Mode.LOCAL;

        OutputStream outVideo, outActivities, outStatistics, outError;
        InputStream inVideo, inActivities, inStatistics, inError;

        videoCirc = new CircularByteBuffer(LOCAL_VIDEO_BUFFER, true);
        activityCirc = new CircularByteBuffer(100 * 1024, true);
        staticticsCirc = new CircularByteBuffer(100 * 1024, true);
        errorCirc = new CircularByteBuffer(100 * 1024, true);

        outVideo = videoCirc.getOutputStream();
        inVideo = videoCirc.getInputStream();

        outActivities = activityCirc.getOutputStream();
        inActivities = activityCirc.getInputStream();

        outStatistics = staticticsCirc.getOutputStream();
        inStatistics = staticticsCirc.getInputStream();

        outError = errorCirc.getOutputStream();
        inError = errorCirc.getInputStream();

        hostVideoOut = outVideo;
        clientVideoIn = inVideo;
        try {
            clientActivitiesOut = new ObjectOutputStream(outActivities);
            hostActivitiesIn = new ObjectInputStream(inActivities);
            clientStatisticsOut = new ObjectOutputStream(outStatistics);
            hostStatisticsIn = new ObjectInputStream(inStatistics);
            hostErrorOut = new ObjectOutputStream(outError);
            clientErrorIn = new ObjectInputStream(inError);
        } catch (IOException e) {
            // ignore, this is local and should not happen
        }

    }

    public OutputStream getVideoOutputStream() {
        return hostVideoOut;
    }

    public ObjectInputStream getActivitiesInputStream() {
        return hostActivitiesIn;
    }

    public ObjectInputStream getDecodingStatisticsInputStream() {
        return hostStatisticsIn;
    }

    public InputStream getVideoInputStream() {
        return clientVideoIn;
    }

    public ObjectOutputStream getActivitiesOutputStream() {
        return clientActivitiesOut;
    }

    public ObjectOutputStream getDecodeStaticticsOutputStream() {
        return clientStatisticsOut;
    }

    public ObjectOutputStream getHostErrorOut() {
        return hostErrorOut;
    }

    public ObjectInputStream getClientErrorIn() {
        return clientErrorIn;
    }

    public Mode getMode() {
        return mode;
    }

    /**
     * Disposes this factory and all it's streams.
     */
    public void dispose() {
        // close streams
        if (mode.equals(Mode.HOST) || mode.equals(Mode.LOCAL)) {
            IOUtils.closeQuietly(hostActivitiesIn);
            IOUtils.closeQuietly(hostStatisticsIn);
            IOUtils.closeQuietly(hostVideoOut);
            IOUtils.closeQuietly(hostErrorOut);
        }
        if (mode.equals(Mode.CLIENT) || mode.equals(Mode.LOCAL)) {
            IOUtils.closeQuietly(clientActivitiesOut);
            IOUtils.closeQuietly(clientStatisticsOut);
            IOUtils.closeQuietly(clientVideoIn);
            IOUtils.closeQuietly(clientErrorIn);
        }
        if (mode.equals(Mode.LOCAL)) {
            videoCirc.clear();
            activityCirc.clear();
            staticticsCirc.clear();
            errorCirc.clear();
            videoCirc = null;
            activityCirc = null;
            staticticsCirc = null;
            errorCirc = null;
        }
    }

}
