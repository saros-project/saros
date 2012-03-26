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
package de.fu_berlin.inf.dpp.videosharing;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * @author s-lau
 */
@Component(module = "net")
public class VideoSharingService extends StreamService {

    protected VideoSharing videoSharing;

    @Override
    public int[] getChunkSize() {
        return new int[] { 1024, 256 };
    }

    @Override
    public int[] getBufferSize() {
        return new int[] { 5 * 1024 * 1024, 100 * 1024 };
    }

    @Override
    public String getServiceName() {
        return "VideoSharing";
    }

    @Override
    public int getStreamsPerSession() {
        return 2;
    }

    @Override
    public long[] getMaximumDelay() {
        return new long[] { 50, 50 };
    }

    @Override
    public boolean sessionRequest(final User from, Object initial) {
        if (!videoSharing.ready())
            return false; // TODO reason to initiator
        return Utils.popUpYesNoQuestion("Incoming screensharing session",
            "Accept screensharing request from " + from.getHumanReadableName()
                + " ?", false);
    }

    @Override
    public void startSession(StreamSession newSession) {
        if (!videoSharing.ready()) {
            newSession.stopSession();
            return; // TODO signal to other user
        }
        videoSharing.startSharing(newSession);
    }

    protected void setVideoSharing(VideoSharing videoSharing) {
        this.videoSharing = videoSharing;
    }

}
