/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.core.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This manager is responsible for distributing knowledge about changes in
 * follow modes between session participants
 */
public class FollowingActivitiesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(FollowingActivitiesManager.class);

    private final List<IFollowModeChangesListener> listeners = new CopyOnWriteArrayList<IFollowModeChangesListener>();

    private final ISarosSession session;

    private final AwarenessInformationCollector collector;

    private final EditorManager editor;

    private final ISharedEditorListener followModeListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User target, boolean isFollowed) {

            if (isFollowed) {
                fireActivity(
                    new StartFollowingActivity(session.getLocalUser(), target));
            } else {
                fireActivity(new StopFollowingActivity(session.getLocalUser()));
            }
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(StartFollowingActivity activity) {
            final User source = activity.getSource();
            final User target = activity.getFollowedUser();

            if (LOG.isDebugEnabled())
                LOG.debug(
                    "received new follow mode from: " + source + " , followed: "
                        + target);

            collector.setUserFollowing(source, target);
            notifyListeners();
        }

        @Override
        public void receive(StopFollowingActivity activity) {
            User source = activity.getSource();

            if (LOG.isDebugEnabled())
                LOG.debug("user " + source + " stopped follow mode");

            collector.setUserFollowing(source, null);
            notifyListeners();
        }
    };

    public FollowingActivitiesManager(final ISarosSession session,
        final AwarenessInformationCollector collector,
        final EditorManager editor) {
        this.session = session;
        this.collector = collector;
        this.editor = editor;
    }

    @Override
    public void start() {
        collector.flushFollowModes();
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);
        editor.addSharedEditorListener(followModeListener);
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);
        editor.removeSharedEditorListener(followModeListener);
        collector.flushFollowModes();
    }

    private void notifyListeners() {
        for (IFollowModeChangesListener listener : listeners)
            listener.followModeChanged();
    }

    public void addListener(IFollowModeChangesListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IFollowModeChangesListener listener) {
        this.listeners.remove(listener);
    }
}
