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
package de.fu_berlin.inf.dpp.videosharing.manager;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.activities.SessionErrorVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.VideoActivity;
import de.fu_berlin.inf.dpp.videosharing.encode.Encoder;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;

/**
 * Receives and responds to the activities of client.
 * 
 * @author s-lau
 * 
 */
public class ActivityManager implements Disposable {

    private static Logger log = Logger.getLogger(ActivityManager.class);

    ImageSource imageSource;
    ObjectInputStream objectIn;
    ActivityManagerReceiver receiver = new ActivityManagerReceiver();
    Thread activityReceiverThread = null;
    VideoSharingSession videoSharingSession;

    protected Encoder encoder;

    public ActivityManager(ImageSource imageSource,
        VideoSharingSession videoSharingSession, ObjectInputStream objectIn) {
        this.imageSource = imageSource;
        this.videoSharingSession = videoSharingSession;
        this.objectIn = objectIn;
        activityReceiverThread = Utils.runSafeAsync(
            "VideoSharing-ActivityReceiver", log, receiver);
    }

    protected class ActivityManagerReceiver implements Runnable {

        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Object o = objectIn.readObject();
                    log.debug(o.toString());
                    if (o instanceof VideoActivity) {
                        VideoActivity activity = (VideoActivity) o;
                        switch (activity.getType()) {
                        case SESSION_STOP:
                            videoSharingSession.dispose();
                            return;
                        case SESSION_PAUSE:
                            videoSharingSession.pause();
                            break;
                        case SESSION_ERROR:
                            if (activity instanceof SessionErrorVideoActivity) {
                                SessionErrorVideoActivity error = (SessionErrorVideoActivity) activity;
                                VideoSharing.reportErrorToUser(
                                    error.getException(),
                                    "Screensharing: Buddy had an exception");
                                videoSharingSession.dispose();
                                return;
                            }
                            break;
                        case IMAGESOURCE_SWITCH_MODE:
                            imageSource.switchMode();
                            break;
                        default:
                            imageSource.processActivity(activity);
                        }

                    }
                } catch (IOException e) {
                    if (!(e instanceof InterruptedIOException))
                        videoSharingSession.reportError(e);
                    return;
                } catch (ClassNotFoundException e) {
                    log.error("Could not find class: ", e);
                    continue;
                }
            }
        }
    }

    public void dispose() {
        // stream will be closed by ConnectionFactory
        if (activityReceiverThread != null)
            activityReceiverThread.interrupt();
    }

}
