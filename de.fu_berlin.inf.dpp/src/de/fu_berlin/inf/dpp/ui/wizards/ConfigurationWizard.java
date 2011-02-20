/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.wizards.pages.StatisticSubmissionWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.CreateXMPPAccountWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.GeneralSettingsWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.IWizardPage2;

/**
 * A wizard to configure Saros (XMPP account, network settings, statistic
 * submission).
 * 
 */
public class ConfigurationWizard extends Wizard {

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    /**
     * We keep our own list of IWizardPage2s so we can call performFinish on
     * them.
     */
    protected List<IWizardPage2> pages = new LinkedList<IWizardPage2>();

    public ConfigurationWizard(boolean askForAccount,
        boolean askAboutStatisticSubmission, boolean showUseNowButton) {

        setWindowTitle("Saros Configuration");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
        setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);

        /*
         * HACK Changing UI components like the configuration wizard should not
         * use PicoContainer directly.
         */
        Saros.injectDependenciesOnly(this);

        if (askForAccount) {
            this.pages.add(new CreateXMPPAccountWizardPage(saros, false,
                showUseNowButton, !showUseNowButton, preferenceUtils));
            this.pages
                .add(new GeneralSettingsWizardPage(saros, preferenceUtils));
        }
        if (askAboutStatisticSubmission) {
            this.pages.add(new StatisticSubmissionWizardPage(statisticManager,
                errorLogManager));
        }
    }

    @Override
    public void addPages() {
        for (IWizardPage2 page : this.pages) {
            addPage(page);
        }
    }

    @Override
    public boolean performFinish() {

        for (IWizardPage2 page : this.pages) {
            if (!page.performFinish()) {
                getContainer().showPage(page);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    /**
     * We override this method, because we only want to allow to finish when the
     * last page is displayed (to make sure that the user saw/confirmed all
     * pages)
     */
    @Override
    public boolean canFinish() {
        return getContainer().getCurrentPage().getNextPage() == null;
    }

}