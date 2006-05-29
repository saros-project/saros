/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.SessionManager;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;

public class SarosUI implements ISessionListener {
    private static final String SESSION_VIEW = "de.fu_berlin.inf.dpp.ui.SessionView";

    public SarosUI(SessionManager sessionManager) {
        sessionManager.addSessionListener(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(final IIncomingInvitationProcess process) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    Shell shell = Display.getDefault().getActiveShell();
                    new WizardDialog(shell, new JoinSessionWizard(process)).open();
                } catch (Exception e) {
                    e.printStackTrace();
                }                
            }
        });
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        Display.getDefault().syncExec(new Runnable() { // show session view
            public void run() {
                try {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    window.getActivePage().showView(SESSION_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
                } catch (PartInitException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @param state
     * @return a nice string description of the given state, which can be used
     * to be shown in labels (e.g. CONNECTING becomes "Connecting..").
     */
    public static String getDescription(ConnectionState state) {
        switch (state) {
            case NOT_CONNECTED:
                return "Not connected";
            case CONNECTING:
                return "Connecting..";
            case CONNECTED:
                return "Connected";
            case DISCONNECTING:
                return "Disconnecting..";
            case ERROR:
                return "Error";
        }
        
        return "";
    }
    
    public static Composite createLabelComposite(Composite parent, String text) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        FillLayout layout = new FillLayout(SWT.NONE);
        layout.marginHeight = 20;
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(text);
      
        return composite;
    }
    
    public static Image getImage(String path) {
        return new Image(Display.getDefault(), SarosUI.getImageDescriptor(path).getImageData());
    }
    
    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("de.fu_berlin.inf.dpp", path);
    }

//    MessageDialog.openInformation(viewer.getControl().getShell(), "Roster View", message);
}
