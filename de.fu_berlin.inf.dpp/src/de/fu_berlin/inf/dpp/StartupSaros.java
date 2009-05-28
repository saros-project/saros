package de.fu_berlin.inf.dpp;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
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
 * @author Lisa Dohrmann
 */
@Component(module = "integration")
public class StartupSaros implements IStartup {

    private Logger log = Logger.getLogger(StartupSaros.class.getName());

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    public StartupSaros() {
        Saros.reinject(this);
    }

    public void earlyStartup() {

        String currentVersion = saros.getVersion();
        String lastVersion = saros.getConfigPrefs().get(
            PreferenceConstants.SAROS_VERSION, "unknown");

        // only continue if version changed
        if (currentVersion.equals(lastVersion)) {
            return;
        }

        saros.getConfigPrefs().put(PreferenceConstants.SAROS_VERSION,
            currentVersion);
        saros.saveConfigPrefs();

        showRoster();
        showConfigurationWizard();
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
                boolean hasAgreement = statisticManager.hasStatisticAgreement();

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
