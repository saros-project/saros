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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.editor.internal.SharedDocumentProvider;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.WizardDialogAccessable;

public class SarosUI implements ISessionListener {

	private static final String SESSION_VIEW = "de.fu_berlin.inf.dpp.ui.SessionView";

	private static final String ROSTER_VIEW = "de.fu_berlin.inf.dpp.ui.RosterView";

	public SarosUI(SessionManager sessionManager) {
		setupCompilationUnitDocumentProvider();
		sessionManager.addSessionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void invitationReceived(final IIncomingInvitationProcess process) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Shell shell = Display.getDefault().getActiveShell();
					JoinSessionWizard jsw = new JoinSessionWizard(process);
					WizardDialogAccessable wd=
						new WizardDialogAccessable(shell, jsw );
					jsw.setWizardDlg(wd);
					process.setInvitationUI(jsw);
					wd.open();
				} catch (Exception e) {
					Saros.getDefault().getLog().log(
						new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Error while joining a session", e));
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					// Create Session View
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					window.getActivePage().showView(SESSION_VIEW, null, IWorkbenchPage.VIEW_CREATE);
				} catch (PartInitException e) {
					Saros.getDefault().getLog().log(
						new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Could not create Session View", e));
				}

				try {
					// Open Roster so that a participant can be invited
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					window.getActivePage()
						.showView(ROSTER_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					Saros.getDefault().getLog().log(
						new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Could not activate Roster View", e));
				}

			}
		});
	}

	/**
	 * @param state
	 * @return a nice string description of the given state, which can be used
	 *         to be shown in labels (e.g. CONNECTING becomes "Connecting..").
	 */
	public static String getDescription(ConnectionState state) {
		switch (state) {
		case NOT_CONNECTED:
			return "Not connected";
		case CONNECTING:
			return "Connecting...";
		case CONNECTED:
			return "Connected (as " + Saros.getDefault().getConnection().getUser() + ")";
		case DISCONNECTING:
			return "Disconnecting...";
		case ERROR:
			return "Error (" + Saros.getDefault().getConnectionError()+ ")";
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
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("de.fu_berlin.inf.dpp", path);
	}

	@SuppressWarnings("restriction")
	private void setupCompilationUnitDocumentProvider() { // UGLY HACK
		CompilationUnitDocumentProvider cuProvider = (CompilationUnitDocumentProvider) JavaPlugin
			.getDefault().getCompilationUnitDocumentProvider();

		SharedDocumentProvider sharedProvider = new SharedDocumentProvider();

		IDocumentSetupParticipant setupParticipant = new JavaDocumentSetupParticipant();
		ForwardingDocumentProvider parentProvider = new ForwardingDocumentProvider(
			IJavaPartitions.JAVA_PARTITIONING, setupParticipant, sharedProvider);

		cuProvider.setParentDocumentProvider(parentProvider);
	}
}
