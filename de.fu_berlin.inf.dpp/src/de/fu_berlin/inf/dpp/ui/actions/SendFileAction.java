/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
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
package de.fu_berlin.inf.dpp.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.sendfile.FileStreamService;
import de.fu_berlin.inf.dpp.util.sendfile.SendFileJob;

/**
 * Action for sending a file among participants.
 * 
 * @author s-lau
 */
public class SendFileAction extends Action implements Disposable {
    private static final Logger log = Logger.getLogger(SendFileAction.class);

    public static final String ACTION_ID = SendFileAction.class.getName();

    protected ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected StreamServiceManager streamServiceManager;

    @Inject
    protected FileStreamService fileStreamService;

    public SendFileAction() {
        super(Messages.SendFileAction_title);
        SarosPluginContext.initComponent(this);

        fileStreamService.hookAction(this);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
        setId(ACTION_ID);
        setToolTipText(Messages.SendFileAction_tooltip);
        setEnabled(false);

        SarosPluginContext.initComponent(this);

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    protected void updateEnablement() {
        List<User> participants = SelectionRetrieverFactory
            .getSelectionRetriever(User.class).getSelection();
        setEnabled(sessionManager.getSarosSession() != null
            && participants.size() == 1 && !participants.get(0).isLocal());
    }

    @Override
    public void run() {
        List<User> participants = null;
        try {
            participants = SelectionRetrieverFactory.getSelectionRetriever(
                User.class).getSelection();
            if (participants.size() != 1) {
                log.warn("More than one participant selected."); //$NON-NLS-1$
                return;
            }
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }

        if (participants == null)
            return;

        // prompt to choose a file
        FileDialog fd = new FileDialog(SWTUtils.getShell(), SWT.OPEN);
        fd.setText(Messages.SendFileAction_filedialog_text);
        String filename = fd.open();
        if (filename == null)
            return;

        // try to access file
        File file = new File(filename);
        InputStream in = null;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            DialogUtils.showErrorPopup(log,
                Messages.SendFileAction_error_cannot_read_file_title,
                MessageFormat.format(
                    Messages.SendFileAction_error_cannot_read_file_message,
                    filename), e, null);
            return;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("unable to close file:" + filename, e); //$NON-NLS-1$
                }
        }

        SendFileJob job = new SendFileJob(streamServiceManager,
            fileStreamService, participants.get(0), file);
        job.setUser(true);
        job.schedule();
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }

}