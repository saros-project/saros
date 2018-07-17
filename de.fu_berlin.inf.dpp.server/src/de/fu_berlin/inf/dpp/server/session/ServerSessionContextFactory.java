package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.server.editor.ServerEditorManager;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.SarosCoreSessionContextFactory;

/**
 * Server implementation of the {@link ISarosSessionContextFactory} interface.
 */
public class ServerSessionContextFactory extends SarosCoreSessionContextFactory {

    @Override
    public final void createNonCoreComponents(ISarosSession session,
        MutablePicoContainer container) {
        container.addComponent(ServerEditorManager.class);
        container.addComponent(FileActivityExecutor.class);
        container.addComponent(FolderActivityExecutor.class);
        container.addComponent(TextEditActivityExecutor.class);
    }
}
