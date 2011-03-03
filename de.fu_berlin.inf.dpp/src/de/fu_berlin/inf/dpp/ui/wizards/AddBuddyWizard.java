/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2011
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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.SubscriptionManager;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.IncomingSubscriptionEvent;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.SubscriptionManagerListener;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.wizards.pages.AddBuddyWizardPage;

/**
 * Wizard for adding a new buddy to the {@link Roster} of the currently
 * connected user.
 * 
 * @author bkahlert
 */
public class AddBuddyWizard extends Wizard {
    private static final Logger log = Logger.getLogger(AddBuddyWizard.class);

    public static final String TITLE = "Add Buddy";
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_BUDDY;

    @Inject
    protected Saros saros;

    @Inject
    protected SubscriptionManager subscriptionManager;

    protected final AddBuddyWizardPage addBuddyWizardPage = new AddBuddyWizardPage();

    /**
     * Caches the {@link JID} reference in case the {@link WizardPage}s are
     * already disposed but a user still needs access.
     */
    protected JID cachedBuddy;

    public AddBuddyWizard() {
        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);

        this.setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        this.addPage(addBuddyWizardPage);
    }

    @Override
    public boolean performFinish() {
        final JID jid = this.addBuddyWizardPage.getBuddy();
        final String nickname = this.addBuddyWizardPage.getNickname();

        if (this.addBuddyWizardPage.isBuddyAlreadyAdded()) {
            log.debug("Buddy " + jid.toString() + " already added.");
            return true;
        }

        /*
         * Listeners that sets the autoSubscribe flag to true and removes itself
         * from the subscriptionManager.
         */
        final SubscriptionManagerListener subscriptionManagerListener = new SubscriptionManagerListener() {
            public void subscriptionReceived(IncomingSubscriptionEvent event) {
                if (jid.equals(event.getBuddy()))
                    event.autoSubscribe = true;
                subscriptionManager.removeSubscriptionManagerListener(this);
            }
        };
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        /*
                         * Register for incoming subscription request from
                         * subscription response.
                         */
                        subscriptionManager
                            .addSubscriptionManagerListener(subscriptionManagerListener);

                        RosterUtils.addToRoster(saros.getConnection(), jid,
                            nickname, SubMonitor.convert(monitor));
                        cachedBuddy = jid;
                    } catch (CancellationException e) {
                        throw new InterruptedException();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            log.warn(e.getCause().getMessage(), e.getCause());

            subscriptionManager
                .removeSubscriptionManagerListener(subscriptionManagerListener);

            this.addBuddyWizardPage.setErrorMessage(e.getMessage());

            // Leave the wizard open
            return false;
        } catch (InterruptedException e) {
            log.debug("Adding buddy " + jid.toString()
                + " was canceled by the user.");

            subscriptionManager
                .removeSubscriptionManagerListener(subscriptionManagerListener);
        }

        // Close the wizard
        return true;
    }

    /*
     * Wizard Results
     */

    /**
     * Returns {@JID} of the newly added buddy
     * 
     * @return
     */
    public JID getBuddy() {
        return this.cachedBuddy;
    }
}
