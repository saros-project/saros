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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.SubscriptionManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.AddBuddyWizardPage;

/**
 * Getting Started Tutorial.
 * 
 * @author bkahlert
 */
public class GettingStartedWizard extends Wizard {
    public static final String TITLE = "Getting Started";
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_BUDDY;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected SubscriptionManager subscriptionManager;

    /**
     * true if the last page should indicate that the Saros Configuration is
     * going to open on finish
     */
    protected boolean showConfigNote;

    protected class GettingStartedPage extends WizardPage {
        protected Image contentImage;

        public GettingStartedPage(String title, String description,
            ImageDescriptor wizban, ImageDescriptor content) {
            super(AddBuddyWizardPage.class.getName());

            setTitle(title);
            setDescription(description);
            setImageDescriptor(wizban);
            this.contentImage = (content != null) ? content.createImage()
                : null;
        }

        /**
         * Paints the {@link #contentImage} so it appears centered and
         * proportionally resized to it always uses the available space. The
         * {@link #contentImage} is never rendered larger than its original
         * size.
         */
        public void createControl(Composite parent) {
            final Composite composite = new Composite(parent, SWT.NONE);
            setControl(composite);

            if (this.contentImage != null) {
                composite.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent e) {
                        Rectangle clientArea = composite.getClientArea();

                        int imgWidth = contentImage.getBounds().width;
                        int imgHeight = contentImage.getBounds().height;

                        int destWidth = imgWidth, destHeight = imgHeight;

                        if (imgWidth > clientArea.width) {
                            destWidth = clientArea.width;
                            destHeight = (int) Math
                                .round(((double) clientArea.width / (double) imgWidth)
                                    * imgHeight);
                            if (destHeight > clientArea.height) {
                                destHeight = clientArea.height;
                                destWidth = (int) Math
                                    .round(((double) clientArea.height / (double) imgHeight)
                                        * imgWidth);
                            }
                        } else if (imgHeight > clientArea.height) {
                            destHeight = clientArea.height;
                            destWidth = (int) Math
                                .round(((double) clientArea.height / (double) imgHeight)
                                    * imgWidth);
                        }

                        e.gc.drawImage(
                            contentImage,
                            0,
                            0,
                            imgWidth,
                            imgHeight,
                            clientArea.x + (clientArea.width - destWidth) / 2,
                            clientArea.y + (clientArea.height - destHeight) / 2,
                            destWidth, destHeight);
                    }
                });
            }
        }

        @Override
        public void dispose() {
            if (this.contentImage != null && !this.contentImage.isDisposed())
                this.contentImage.dispose();
            super.dispose();
        }
    }

    /**
     * Constructs the Getting Started Tutorial
     * 
     * @param showConfigNote
     *            true if the last page should indicate that the Saros
     *            Configuration is going to open on finish
     */
    public GettingStartedWizard(boolean showConfigNote) {
        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);
        this.setNeedsProgressMonitor(false);

        this.showConfigNote = showConfigNote;
    }

    @Override
    public void addPages() {
        this.addPage(new GettingStartedPage("Getting Started", null,
            ImageManager.WIZBAN_GETTING_STARTED_STEP0,
            ImageManager.IMAGE_GETTING_STARTED_STEP0));
        this.addPage(new GettingStartedPage(
            "Step 1: Configure Your Account",
            "The Saros configuration wizard will help you set your Jabber/XMPP account.",
            ImageManager.WIZBAN_GETTING_STARTED_STEP1,
            ImageManager.IMAGE_GETTING_STARTED_STEP1));
        this.addPage(new GettingStartedPage(
            "Step 2: Add Your Buddies",
            "You will need to add buddies to your buddy list in order to work with them.",
            ImageManager.WIZBAN_GETTING_STARTED_STEP2,
            ImageManager.IMAGE_GETTING_STARTED_STEP2));
        this.addPage(new GettingStartedPage("Step 3: Start Working Together",
            "Saros offers you many ways to start a collaboration.",
            ImageManager.WIZBAN_GETTING_STARTED_STEP3,
            ImageManager.IMAGE_GETTING_STARTED_STEP3));
        this.addPage(new GettingStartedPage("Finish", null,
            ImageManager.WIZBAN_GETTING_STARTED_STEP4,
            (showConfigNote) ? ImageManager.IMAGE_GETTING_STARTED_STEP4_CONFIG
                : ImageManager.IMAGE_GETTING_STARTED_STEP4_NOCONFIG));
    }

    @Override
    public boolean canFinish() {
        return this.getContainer().getCurrentPage().getNextPage() == null;
    }

    @Override
    public boolean performFinish() {
        preferenceUtils.setGettingStartedFinished(true);
        return true;
    }

    @Override
    public boolean performCancel() {
        if (showConfigNote) {
            boolean finished = !DialogUtils
                .openQuestionMessageDialog(
                    this.getShell(),
                    "Show Again?",
                    "Do you want this tutorial to show up again on your next start of Eclipse?\n\n"
                        + "If not you have always the possiblity to start the tutorial via the Saros main menu.");
            preferenceUtils.setGettingStartedFinished(finished);
        }
        return true;
    }
}
