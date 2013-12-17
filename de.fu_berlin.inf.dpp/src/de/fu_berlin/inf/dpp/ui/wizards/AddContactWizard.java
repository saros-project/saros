/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2011
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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.AddContactWizardPage;

/**
 * Wizard for adding a new contact to the {@link Roster roster} of the currently
 * connected user.
 * 
 * @author bkahlert
 */
public class AddContactWizard extends Wizard {
    private static final Logger log = Logger.getLogger(AddContactWizard.class);

    public static final String TITLE = Messages.AddContactWizard_title;
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_CONTACT;

    @Inject
    protected SarosNet sarosNet;

    @Inject
    protected SubscriptionHandler subscriptionManager;

    protected final AddContactWizardPage addContactWizardPage = new AddContactWizardPage();

    /**
     * Caches the {@link JID} reference in case the {@link WizardPage}s are
     * already disposed but a user still needs access.
     */
    protected JID cachedContact;

    public AddContactWizard() {
        SarosPluginContext.initComponent(this);
        setWindowTitle(TITLE);
        setDefaultPageImageDescriptor(IMAGE);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        addPage(addContactWizardPage);
    }

    @Override
    public boolean performFinish() {
        final JID jid = addContactWizardPage.getContact();
        final String nickname = addContactWizardPage.getNickname();

        if (addContactWizardPage.isContactAlreadyAdded()) {
            log.debug("contact " + jid.toString() + " already added");
            return true;
        }

        try {
            getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {

                    monitor.beginTask("Adding contact " + jid + "...",
                        IProgressMonitor.UNKNOWN);

                    try {
                        RosterUtils.addToRoster(sarosNet.getConnection(), jid,
                            nickname);

                        cachedContact = jid;
                    } catch (CancellationException e) {
                        throw new InterruptedException();
                    } catch (XMPPException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            log.warn(e.getCause().getMessage(), e.getCause());
            addContactWizardPage.setErrorMessage(e.getMessage());
            // Leave the wizard open
            return false;
        } catch (InterruptedException e) {
            log.error("uninterruptable context was interrupted", e);
        }

        // Close the wizard
        return true;
    }

    /**
     * Returns {@JID} of the newly added contact
     * 
     * @return
     */
    public JID getContact() {
        return cachedContact;
    }
}
