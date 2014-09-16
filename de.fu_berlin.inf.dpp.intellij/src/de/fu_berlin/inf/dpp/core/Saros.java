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

package de.fu_berlin.inf.dpp.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.core.context.SarosContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.helpers.LogLog;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

/**
 * Saros plugin class for bundling globally necessary variables like project.
 */
public class Saros {

    /**
     * This is the plugin ID that identifies the saros plugin in the IDEA ecosystem.
     */
    public static final String PLUGIN_ID = "de.fu_berlin.inf.dpp.intellij";

    private static Saros instance;

    private static boolean isInitialized;

    private Project project;

    private ToolWindow toolWindow;

    private XMPPConnectionService connectionService;
    private PreferenceUtils preferenceUtils;

    //FIXME: Add again when SarosMainPanelView was added
    //private SarosMainPanelView mainPanel;
    private IWorkspace workspace;

    private SarosContext sarosContext;

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Checks if Saros was already initialized by create(). Throws an
     * IllegalStateException if not initialized.
     *
     * @throws IllegalStateException
     */
    public static void checkInitialized() {
        if (!isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    /**
     * Creates a new Saros singleton instance from a project.
     *
     * @param project
     * @return
     */
    public synchronized static Saros create(Project project) {
        if (instance == null) {
            instance = new Saros(project);
            instance.start();
        }
        return instance;
    }

    private Saros(Project project) {
        this.project = project;
        this.workspace = new Workspace(project);
    }

    /**
     * If not initialized yet, this method initializes fields, the SarosPluginContext and the XMPPConnectionService.
     */
    public void start() {

        if (isInitialized) {
            return;
        }

        //CONTEXT
        sarosContext = new SarosContext(new SarosIntellijContextFactory(this,
            new SarosCoreContextFactory()), new DotGraphMonitor()
        );

        SarosPluginContext.setSarosContext(sarosContext);

        connectionService = sarosContext
            .getComponent(XMPPConnectionService.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);

        connectionService.addListener(new IConnectionListener() {

            @Override
            public void connectionStateChanged(Connection connection,
                ConnectionState state) {

                if (state == ConnectionState.CONNECTING) {
                    ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
                        SarosConstants.XMPP_FEATURE_NAMESPACE);
                }
            }

        });

        //todo: set parameters from config
        connectionService.configure(SarosConstants.RESOURCE, false, false, 8888,
                null, null, true, null, 80, true);

        isInitialized = true;
        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);
    }

    //FIXME: Properly stop network and context classes
    void stop() {
        isInitialized = false;
    }

    public Project getProject() {
        return project;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    //FIXME: Add again when SarosMainPanelView was added.
    /*
    public SarosMainPanelView getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(SarosMainPanelView mainPanel) {
        this.mainPanel = mainPanel;
    }*/
}

