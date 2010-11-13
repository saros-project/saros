package de.fu_berlin.inf.dpp;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosControler;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * An instance of this class is instantiated when Eclipse starts, after the
 * Saros plugin has been started.
 * 
 * {@link #earlyStartup()} is called after the workbench is initialized. <br>
 * <br>
 * Checks whether the release number changed.
 * 
 * @author Lisa Dohrmann, Sandor Sz√ºcs
 */
@Component(module = "integration")
public class StartupSaros implements IStartup {

    private static final Logger log = Logger.getLogger(StartupSaros.class);

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected EditorManager editorManager;

    public StartupSaros() {
        Saros.reinject(this);
    }

    public void earlyStartup() {
        String currentVersion = saros.getVersion();
        String lastVersion = saros.getConfigPrefs().get(
            PreferenceConstants.SAROS_VERSION, "unknown");

        String portNumber = System.getProperty("de.fu_berlin.inf.dpp.testmode");
        String sleepTime = System.getProperty("de.fu_berlin.inf.dpp.sleepTime");
        log.debug("de.fu_berlin.inf.dpp.testmode=" + portNumber);

        boolean testmode = portNumber != null;

        if (testmode) {
            int port = Integer.parseInt(portNumber);
            int time = Integer.parseInt(sleepTime);
            log.info("entered testmode, start RMI bot listen on port " + port);
            log.info("sleep time: " + sleepTime);
            startRmiBot(port, time);
        }

        boolean assertEnabled = false;

        // Side-effect-full assert to set assertEnabled to true if -ea
        assert true == (assertEnabled = true);

        // only continue if version changed or if -ea (for testing)
        if (currentVersion.equals(lastVersion) || assertEnabled) {
            return;
        }

        saros.getConfigPrefs().put(PreferenceConstants.SAROS_VERSION,
            currentVersion);
        saros.saveConfigPrefs();

        showRoster();
        showConfigurationWizard();
    }

    protected void startRmiBot(final int port, final int time) {
        log.info("start RMI Bot");
        Util.runSafeAsync("RmiSWTWorkbenchBot-", log, new Runnable() {
            public void run() {
                log.debug("Util.isSWT(): " + Util.isSWT());
                SarosControler bot = SarosControler.getInstance();
                bot.sleepTime = time;
                try {
                    bot.initExportedObjects(port, saros, sessionManager, dataTransferManager,
                        editorManager);
                    bot.listRmiObjects();
                } catch (RemoteException e) {
                    log.error("remote:", e);
                }
            }
        });
    }

    protected void showRoster() {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                IIntroManager m = PlatformUI.getWorkbench().getIntroManager();
                IIntroPart i = m.getIntro();
                /*
                 * if there is a welcome screen, don't activate the Roster
                 * because it would be maximized and hiding the workbench window
                 */
                if (i != null)
                    return;
                sarosUI.activateRosterView();
            }
        });
    }

    protected void showConfigurationWizard() {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                // determine which pages have to be shown
                boolean hasUsername = preferenceUtils.hasUserName();
                boolean hasAgreement = statisticManager.hasStatisticAgreement()
                    && errorLogManager.hasErrorLogAgreement();

                if (!hasUsername || !hasAgreement) {
                    Wizard wiz = new ConfigurationWizard(!hasUsername,
                        !hasAgreement);
                    WizardDialog dialog = new WizardDialog(
                        EditorAPI.getShell(), wiz);
                    dialog.open();
                }
            }
        });
    }
}
