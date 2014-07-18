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

package de.fu_berlin.inf.dpp.intellij.context;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.filesystem.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFileContentChangedNotifier;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileContentChangedNotifierBridge;
import de.fu_berlin.inf.dpp.intellij.store.PreferenceStore;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.util.Arrays;

/**
 * IntelliJ related context
 */
public class SarosIntellijContextFactory extends AbstractSarosContextFactory {

    private Saros saros;

    private final ISarosContextFactory additionalContext;

    //FIXME: uncomment when classes are submitted
    private final Component[] components = new Component[] {

        // Component.create(ISarosSessionManager.class, SarosSessionManager.class), //todo
        // Core Managers
        // Component.create(ConsistencyWatchdogClient.class), //todo

        Component.create(EditorAPI.class), Component.create(ProjectAPI.class),

        Component.create(EditorManager.class),
        Component.create(LocalEditorHandler.class),
        Component.create(LocalEditorManipulator.class),

        // UI handlers
        // Component.create(NegotiationHandler.class), //todo
        // Component.create(UserStatusChangeHandler.class), //todo
        // Component.create(JoinSessionRequestHandler.class), //todo
        // Component.create(JoinSessionRejectedHandler.class), //todo
        // Component.create(ServerPreferenceHandler.class),  //todo
        // Component.create(SessionStatusRequestHandler.class), //todo
        // Component.create(XMPPAuthorizationHandler.class), //todo

        Component.create(IChecksumCache.class, ChecksumCacheImpl.class),

        // Component.create(UISynchronizer.class, IntelliJSynchronizer.class), //todo

        Component.create(IFileContentChangedNotifier.class,
            FileContentChangedNotifierBridge.class),

        Component.create(PreferenceUtils.class),

        // Component.create(FollowModeAction.class), //todo
        // Component.create(LeaveSessionAction.class),  //todo
    };

    public SarosIntellijContextFactory(Saros saros,
        ISarosContextFactory delegate) {
        this.saros = saros;
        this.additionalContext = delegate;
    }

    @Override
    public void createComponents(MutablePicoContainer container) {

        if (additionalContext != null) {
            additionalContext.createComponents(container);
        }

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }

        container.addComponent(saros);
        container.addComponent(IPreferenceStore.class, new PreferenceStore());

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.SarosVersion.class), "14.1.31.DEVEL"
        );  //todo

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.PlatformVersion.class), "4.3.2"
        ); //todo

    }
}
