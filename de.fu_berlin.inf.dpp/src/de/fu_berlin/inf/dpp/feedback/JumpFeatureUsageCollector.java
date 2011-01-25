/*
 * DPP - Serious Distributed Pair Programming
 * (c) Moritz v. Hoffen, Freie Universitaet Berlin 2010
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

package de.fu_berlin.inf.dpp.feedback;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * This Collector collects data about the jump feature usage. It stores data
 * about the total count of jumps performed as well as the jumps to a user with
 * {@link User.Permission#WRITE_ACCESS} or
 * {@link User.Permission#READONLY_ACCESS}.
 */
@Component(module = "feedback")
public class JumpFeatureUsageCollector extends AbstractStatisticCollector {

    protected static final Logger log = Logger
        .getLogger(PermissionChangeCollector.class.getName());
    protected int jumpedToWriteAccessHolder = 0;
    protected int jumpedToReadOnlyAccessHolder = 0;

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void jumpedToUser(User jumpedTo) {
            if (jumpedTo.hasWriteAccess()) {
                jumpedToWriteAccessHolder++;
            } else {
                jumpedToReadOnlyAccessHolder++;
            }
        }
    };

    public JumpFeatureUsageCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager, EditorManager editorManager) {
        super(statisticManager, sessionManager);
        editorManager.addSharedEditorListener(editorListener);
    }

    @Override
    protected void processGatheredData() {
        // write counts to statistics
        data.setJumpedToUserWithReadOnlyAccessCount(jumpedToReadOnlyAccessHolder);
        data.setJumpedToUserWithWriteAccessCount(jumpedToWriteAccessHolder);
        data.setJumpedToCount(jumpedToWriteAccessHolder
            + jumpedToReadOnlyAccessHolder);
    }

    @Override
    protected void clearPreviousData() {
        // set the counts to null again
        jumpedToWriteAccessHolder = 0;
        jumpedToReadOnlyAccessHolder = 0;

        super.clearPreviousData();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        // nothing to be done
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        // nothing to be done
    }

}
