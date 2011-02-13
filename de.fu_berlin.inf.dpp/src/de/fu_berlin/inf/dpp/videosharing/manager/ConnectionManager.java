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
package de.fu_berlin.inf.dpp.videosharing.manager;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.encode.Encoder;

/**
 * Monitors the videostream and reduces consumed bandwidth when connection
 * worses. But this is currently not implemented.
 * 
 * @author s-lau
 * 
 */
public class ConnectionManager implements Disposable {

    private static Logger log = Logger.getLogger(ConnectionManager.class);

    @Inject
    protected Saros saros;

    protected OutputStream out;
    protected ObjectInputStream decodeStatistics;
    protected Encoder encoder;
    protected EncoderManager encoderManager;
    protected Thread encoderManagerThread;
    protected DecodingStatisticManager decodingStatisticManager;
    protected Thread decodingStatisticManagerThread;
    protected VideoSharingSession videoSharingSession;

    public ConnectionManager(OutputStream out, Encoder encoder,
        ObjectInputStream decodeStatistics,
        VideoSharingSession videoSharingSession) {
        this.out = out;
        this.encoder = encoder;
        this.decodeStatistics = decodeStatistics;
        this.videoSharingSession = videoSharingSession;
        this.encoderManager = new EncoderManager();
        this.decodingStatisticManager = new DecodingStatisticManager();
        encoderManagerThread = Utils.runSafeAsync("EncoderManager", log,
            encoderManager);
        decodingStatisticManagerThread = Utils.runSafeAsync(
            "DecodingStatisticManager", log, decodingStatisticManager);
    }

    public void dispose() {
        // streams will be closed by ConnectionFactory
        encoderManagerThread.interrupt();
        decodingStatisticManagerThread.interrupt();
    }

    public class DecodingStatisticManager implements Runnable {

        public void run() {
            while (true && !Thread.interrupted()) {
                try {
                    log.debug(decodeStatistics.readObject());
                } catch (IOException e) {
                    if (!(e instanceof InterruptedIOException))
                        videoSharingSession.reportError(e);
                    return;
                } catch (ClassNotFoundException e) {
                    log.error("Received unknown Object: ", e);
                    continue;
                }
            }
        }

    }

    public class EncoderManager implements Runnable {

        public void run() {
            if (!encoder.isEncoding())
                try {
                    encoder.waitForStartEncoding();
                } catch (InterruptedException e1) {
                    return;
                }
            // TODO eval statistics and control encoder
        }

    }

}
