package de.fu_berlin.inf.dpp.concurrent.watchdog;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.test.fakes.EclipseWorkspaceFakeFacade;
import de.fu_berlin.inf.dpp.test.util.SarosTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EditorManager.class, FileEditorInput.class })
public class ConsistencyWatchdogServerTest {

    private static IWorkspace workspace;

    @BeforeClass
    public static void createWorkspace() {
        workspace = EclipseWorkspaceFakeFacade.createWorkspace("workspace");
    }

    @Test
    public void testEqualChecksums() throws Exception {

        FileEditorInput fileEditorInputMock = EasyMock
            .createMock(FileEditorInput.class);

        IDocumentProvider documentProviderMock = EasyMock
            .createNiceMock(IDocumentProvider.class);

        IDocument documentMock = EasyMock.createMock(IDocument.class);

        EasyMock.expect(
            documentProviderMock.getDocument(EasyMock
                .isA(FileEditorInput.class))).andReturn(documentMock);

        documentProviderMock.connect(EasyMock.isA(FileEditorInput.class));
        EasyMock.expectLastCall();

        documentProviderMock.disconnect(EasyMock.isA(FileEditorInput.class));
        EasyMock.expectLastCall();

        PowerMock.mockStaticPartial(EditorManager.class, "getDocumentProvider");

        EditorManager.getDocumentProvider(EasyMock.isA(FileEditorInput.class));
        PowerMock.expectLastCall().andReturn(documentProviderMock).anyTimes();

        PowerMock.expectNew(FileEditorInput.class, EasyMock.isA(IFile.class))
            .andReturn(fileEditorInputMock);

        SarosSessionManager sarosSessionManagerMock = EasyMock
            .createMock(SarosSessionManager.class);

        sarosSessionManagerMock.addSarosSessionListener(EasyMock
            .isA(ISarosSessionListener.class));

        EasyMock.expectLastCall();

        ISarosSession sarosSessionMock = EasyMock
            .createMock(ISarosSession.class);

        EasyMock.expect(sarosSessionMock.getLocalUser()).andReturn(
            new User(sarosSessionMock, new JID("foo@bar"), 0));

        sarosSessionMock.activityCreated(EasyMock.isA(ChecksumActivity.class));
        EasyMock.expectLastCall();

        PowerMock.replayAll(sarosSessionManagerMock, sarosSessionMock,
            documentProviderMock, fileEditorInputMock /* documentMock */);

        IProject project = workspace.getRoot().getProject("foo");
        IFile file = project.getFile("src/Person.java");

        file.create(new ByteArrayInputStream(new byte[0]), true,
            SarosTestUtils.submonitor());

        // System.out.println(file.exists());
        Set<SPath> missingDocuments = new HashSet<SPath>();
        Set<SPath> localEditors = new HashSet<SPath>();
        Set<SPath> remoteEditors = new HashSet<SPath>();

        localEditors.add(new SPath(project.getFile("src/Person.java")));
        remoteEditors.add(new SPath(project.getFile("src/Person.java")));

        missingDocuments.addAll(localEditors);
        missingDocuments.addAll(remoteEditors);

        ConsistencyWatchdogServer watchdog = new ConsistencyWatchdogServer(
            sarosSessionManagerMock);

        watchdog.sarosSession = sarosSessionMock;

        watchdog.updateChecksum(missingDocuments, localEditors, remoteEditors,
            new SPath(file));

        // PowerMock.verifyAll();

        assertEquals(0, missingDocuments.size());

    }
}
