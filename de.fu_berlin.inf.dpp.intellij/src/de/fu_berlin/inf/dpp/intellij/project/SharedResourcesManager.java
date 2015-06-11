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

package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.VCSActivity;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJWorkspaceImpl;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * The SharedResourcesManager creates and handles file and folder activities.
 */
public class SharedResourcesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(SharedResourcesManager.class);

    private final ISarosSession sarosSession;

    private final FileSystemChangeListener fileSystemListener;

    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

    private final LocalEditorHandler localEditorHandler;

    private final LocalEditorManipulator localEditorManipulator;

    private final IntelliJWorkspaceImpl intelliJWorkspaceImpl;

    @Override
    public void start() {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {

            @Override public void run() {
                sarosSession.addActivityProducer(SharedResourcesManager.this);
                sarosSession.addActivityConsumer(consumer);
                intelliJWorkspaceImpl.addResourceListener(fileSystemListener);

            }
        }, ModalityState.any());
    }

    @Override
    public void stop() {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {

            @Override public void run() {
                intelliJWorkspaceImpl.removeResourceListener(fileSystemListener);
                sarosSession
                    .removeActivityProducer(SharedResourcesManager.this);
                sarosSession.removeActivityConsumer(consumer);
            }
        }, ModalityState.any());
    }

    public SharedResourcesManager(ISarosSession sarosSession,
        EditorManager editorManager,
        FileReplacementInProgressObservable fileReplacementInProgressObservable,
        LocalEditorHandler localEditorHandler,
        LocalEditorManipulator localEditorManipulator, IntelliJWorkspaceImpl intelliJWorkspaceImpl) {

        this.sarosSession = sarosSession;
        this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
        this.localEditorHandler = localEditorHandler;
        this.localEditorManipulator = localEditorManipulator;
        fileSystemListener = new FileSystemChangeListener(this, editorManager);
        this.intelliJWorkspaceImpl = intelliJWorkspaceImpl;
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            if (!(activity instanceof FileActivity
                || activity instanceof FolderActivity
                || activity instanceof VCSActivity))
                return;

        /*
         * FIXME this will lockout everything. File changes made in the
         * meantime from another background job are not recognized. See
         * AddMultipleFilesTest STF test which fails randomly.
         */
            fileReplacementInProgressObservable.startReplacement();
            fileSystemListener.setEnabled(false);
            super.exec(activity);

            LOG.trace("execing " + activity + " in " + Thread.currentThread()
                .getName());

            fileReplacementInProgressObservable.replacementDone();
            fileSystemListener.setEnabled(true);
            LOG.trace("done execing " + activity);
        }

        @Override
        public void receive(FileActivity activity) {
            try {
                handleFileActivity(activity);
            } catch (IOException e) {
                LOG.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(FolderActivity activity) {
            try {
                handleFolderActivity(activity);
            } catch (IOException e) {
                LOG.error("Failed to execute activity: " + activity, e);
            }
        }
    };

    private void handleFileActivity(FileActivity activity) throws IOException {

        if (activity.isRecovery()) {
            handleFileRecovery(activity);
            return;
        }

        // TODO check if we should open / close existing editors here too
        switch (activity.getType()) {
        case CREATED:
            handleFileCreation(activity);
            break;
        case REMOVED:
            handleFileDeletion(activity);
            break;
        case MOVED:
            handleFileMove(activity);
            break;
        }
    }

    private void handleFileRecovery(FileActivity activity) throws IOException {
        SPath path = activity.getPath();

        LOG.debug("performing recovery for file: " + activity.getPath()
            .getFullPath());

        FileActivity.Type type = activity.getType();

        try {
            if (type == FileActivity.Type.CREATED) {
                handleFileCreation(activity);
            } else if (type == FileActivity.Type.REMOVED) {
                localEditorManipulator.closeEditor(path);
                handleFileDeletion(activity);
            } else {
                LOG.warn("performing recovery for type " + type
                    + " is not supported");
            }
        } finally {
            /*
             * always reset Jupiter or we will get into trouble because the
             * vector time is already reseted on the host
             */
            sarosSession.getConcurrentDocumentClient().reset(path);
        }
    }

    private void handleFileMove(FileActivity activity) throws IOException {
        IPath newFilePath = activity.getPath().getFullPath();
        IResource oldResource = activity.getOldPath().getResource();

        FileUtils.mkdirs(activity.getPath().getResource());
        FileUtils.move(newFilePath, oldResource);

        if (activity.getContent() == null)
            return;

        handleFileCreation(activity);
    }

    private void handleFileDeletion(FileActivity activity) throws IOException {
        IFile file = activity.getPath().getFile();

        if (!file.exists()) {
            LOG.warn(
                "could not delete file " + file + " because it does not exist");
        }

        try {
            fileSystemListener.setEnabled(false);
            FileUtils.delete(file);
            //HACK: It does not work to disable the fileSystemListener temporarly,
            //because a fileCreated event will be fired asynchronously,
            //so we have to add this file to the filter list
            fileSystemListener
                .addIncomingFileToFilterFor(file.getFullPath().toFile());
        } finally {
            fileSystemListener.setEnabled(true);
        }
    }

    private void handleFileCreation(FileActivity activity) throws IOException {

        String encodingString = activity.getEncoding() == null ?
            EncodingProjectManager.getInstance().getDefaultCharset().name() :
            activity.getEncoding();

        //FIXME: Test if updateEncoding method will be necessary
        String newText = new String(activity.getContent(), encodingString);

        //if the file exists, try to replace the content completely
        if (!newText.isEmpty()) {
            //this is true only when the file already existed
            boolean replaceSuccessful = localEditorManipulator
                .replaceText(activity.getPath(), newText);

            if (replaceSuccessful) {
                //If the content of the existing document was replaced
                //successfully, save the file
                localEditorHandler.saveFile(activity.getPath());
                return;
            }
        }

        //If the file did not exist, create it
        IFile file = activity.getPath().getFile();
        byte[] actualContent = FileUtils.getLocalFileContent(file);
        byte[] newContent = activity.getContent();

        if (Arrays.equals(newContent, actualContent)) {
            LOG.info("FileActivity " + activity + " dropped (same content)");
            return;
        }

        try {
            fileSystemListener.setEnabled(false);
            FileUtils
                .writeFile(new ByteArrayInputStream(newContent), file, null);
            //HACK: It does not work to disable the fileSystemListener temporarily,
            //because a fileCreated event will be fired asynchronously,
            //so we have to add this file to the filter list
            fileSystemListener
                .addIncomingFileToFilterFor(file.getFullPath().toFile());
        } finally {
            fileSystemListener.setEnabled(true);
        }
    }

    private void handleFolderActivity(FolderActivity activity)
        throws IOException {

        SPath path = activity.getPath();

        IFolder folder = path.getProject()
            .getFolder(path.getProjectRelativePath());
        fileSystemListener.setEnabled(false);
        //HACK: It does not work to disable the fileSystemListener temporarly,
        //because a fileCreated event will be fired asynchronously,
        //so we have to add this file to the filter list
        try {
            if (activity.getType() == FolderActivity.Type.CREATED) {
                FileUtils.create(folder);
            } else if (activity.getType() == FolderActivity.Type.REMOVED) {

                if (folder.exists()) {
                    FileUtils.delete(folder);
                }

            }
            fileSystemListener
                .addIncomingFileToFilterFor(folder.getFullPath().toFile());
        } finally {
            fileSystemListener.setEnabled(true);
        }
    }

    void internalFireActivity(IActivity activity) {
        fireActivity(activity);
    }

    public ISarosSession getSession() {
        return sarosSession;
    }
}