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
import de.fu_berlin.inf.dpp.core.context.SarosContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/**
 * Saros plugin class
 */
public class Saros {

    private static final Logger LOG = Logger.getLogger(Saros.class);

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */

    public static final String SAROS = "de.fu_berlin.inf.dpp";

    /**
     * Default server name
     */
    public static final String SAROS_SERVER = "saros-con.imp.fu-berlin.de";

    /**
     * The name of the XMPP namespace used by SarosEclipse. At the moment it is only
     * used to advertise the SarosEclipse feature in the Service Discovery.
     * <p/>
     * TODO Add version information, so that only compatible versions of SarosEclipse
     * can use each other.
     */
    public static final String NAMESPACE = SAROS;

    /**
     * The name of the resource identifier used by Saros when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, Saros will
     * connect using john@doe.com/Saros)
     * <p/>
     */
    public static final String RESOURCE = "Saros";

    /**
     * Sub-namespace for the server. It is used to advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server";

    private static Saros instance;

    private static boolean isInitialized;

    private Project project;
    private ToolWindow toolWindow;

    protected PreferenceUtils preferenceUtils;

    //FIXME: Add again when SarosMainPanelView was added
    //private SarosMainPanelView mainPanel;
    private IWorkspace workspace;

    private SarosContext sarosContext;

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link de.fu_berlin.inf.dpp.core.context.SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Checks if Saros was already initialized by create(). Throws an
     * IllegalStateException if not initialized.
     */
    public static void checkInitialized() {
        if (!isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    //FIXME: Add again when SarosMainPanelView was added
    /*
    public SarosMainPanelView getMainPanel()
    {
        return mainPanel;
    }

    public void setMainPanel(SarosMainPanelView mainPanel)
    {
        this.mainPanel = mainPanel;
    }*/

    /**
     * Creates a new Saros singleton instance with a project.
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

    /**
     * @return the Saros instance
     */
    public synchronized static Saros getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Saros not initialized");
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
        sarosContext = new SarosContext(
            new SarosIntellijContextFactory(this,
                new SarosCoreContextFactory()), new DotGraphMonitor()
        );

        SarosPluginContext.setSarosContext(sarosContext);

        XMPPConnectionService connectionService = sarosContext
            .getComponent(XMPPConnectionService.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);

        //todo: set parameters from config
        connectionService
            .configure(Saros.NAMESPACE, Saros.RESOURCE, false, false, 8888,
                null, null, true, null, 80, true);

        isInitialized = true;
        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);

    }

    //TODO: Properly stop network and context classes
    void stop() {
        isInitialized = false;
    }

    public Project getProject() {
        return project;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public SarosContext getSarosContext() {
        return sarosContext;
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }

}
