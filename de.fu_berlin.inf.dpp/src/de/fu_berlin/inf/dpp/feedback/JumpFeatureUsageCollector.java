/*
 * DPP - Serious Distributed Pair Programming
 * (c) Moritz v. Hoffen, Freie Universität Berlin 2010
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

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * This Collector collects data about the jump feature usage. It stores data
 * about the total count of jumps performed as well as the jumps to a user with
 * {@link Permission#WRITE_ACCESS} or {@link Permission#READONLY_ACCESS}.
 */
@Component(module = "feedback")
public class JumpFeatureUsageCollector extends AbstractStatisticCollector {

    protected int jumpedToWriteAccessHolder = 0;
    protected int jumpedToReadOnlyAccessHolder = 0;

    private final IEditorManager editorManager;

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void jumpedToUser(User target) {
            if (target.hasWriteAccess()) {
                jumpedToWriteAccessHolder++;
            } else {
                jumpedToReadOnlyAccessHolder++;
            }
        }
    };

    public JumpFeatureUsageCollector(StatisticManager statisticManager,
        ISarosSession session, IEditorManager editorManager) {
        super(statisticManager, session);
        this.editorManager = editorManager;
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
    protected void doOnSessionStart(ISarosSession sarosSession) {
        editorManager.addSharedEditorListener(editorListener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        editorManager.removeSharedEditorListener(editorListener);
    }

}
