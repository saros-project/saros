package de.fu_berlin.inf.dpp.invitation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.Path;
import org.junit.Before;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;

public class AbstractFileListTest {
    /*
     * TODO Currently, the subclasses of this test both FileList[Diff] and
     * FileListFactory, which have two very different jobs. The test logic for
     * FileList[Diff] may mainly rely on plain String inputs, and test the
     * storing/sorting/retrieving capabilities of the classes under test. The
     * FileListFactory, in contrast, needs to be tested with regard to its
     * FileList creation capabilities.
     */

    protected static final String ROOT1 = "root1";
    protected static final String ROOT2 = "root2";
    protected static final String SUBDIR_FILE1 = "subdir/file1";
    protected static final String SUBDIR_FILE2 = "subdir/file2";

    protected static IProject project;

    static {
        project = EasyMock.createNiceMock(IProject.class);
        EasyMock.expect(project.getName()).andStubReturn("Foo");

        try {
            EasyMock.expect(project.getDefaultCharset()).andStubReturn(
                "US-ASCII");
        } catch (IOException e) {
            // cannot happen as the mock is in recording mode
        }

        EasyMock.replay(project);
    }

    // needed as the mocks will always return the same input stream instance
    private static class ResetingInputStream extends InputStream {

        private final String content;
        private ByteArrayInputStream in;

        public ResetingInputStream(String content) {
            this.content = content;
            in = new ByteArrayInputStream(this.content.getBytes());
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public void close() throws IOException {
            in = new ByteArrayInputStream(this.content.getBytes());
        }
    }

    protected IFile fileInRoot1;
    protected IFile fileInRoot2;
    protected IFile fileInSubDir1;
    protected IFile fileInSubDir2;
    protected IFile fileInSubDir1changed;

    protected List<IResource> threeFileList = new ArrayList<IResource>();

    /**
     * Entries: <code>root1, root2, subdir/file1</code>
     */
    protected FileList threeEntryList;

    /**
     * Entries: <code>root1, root2, subdir/file1, subdir/file2</code>
     */
    protected FileList fourEntryList;

    /**
     * same as {@link #fourEntryList}, but <code>subdir/file1</code> has a
     * different content
     */
    protected FileList modifiedFourEntryList;
    protected FileList emptyFileList;

    @Before
    public void setUp() throws Exception {
        fileInRoot1 = createFileMock(ROOT1, "fileInRoot1", "ISO-8859-1");
        fileInRoot2 = createFileMock(ROOT2, "fileInRoot2", "ISO-8859-1");
        fileInSubDir1 = createFileMock(SUBDIR_FILE1, "fileInSubDir1", "UTF-16");
        fileInSubDir2 = createFileMock(SUBDIR_FILE2, "fileInSubDir2", "UTF-16");
        fileInSubDir1changed = createFileMock(SUBDIR_FILE1,
            "changed fileInSubDir1", "UTF-16");

        List<IResource> resources = new ArrayList<IResource>();
        resources.add(fileInRoot1);
        resources.add(fileInRoot2);
        resources.add(fileInSubDir1);
        threeFileList.addAll(resources);

        threeEntryList = FileListFactory.createFileList(null, resources, null,
            null, null);

        resources.add(fileInSubDir2);

        fourEntryList = FileListFactory.createFileList(null, resources, null,
            null, null);

        resources.remove(fileInSubDir1);
        resources.add(fileInSubDir1changed);

        modifiedFourEntryList = FileListFactory.createFileList(null, resources,
            null, null, null);

        emptyFileList = new FileList();
    }

    protected IFile createFileMock(String path, String content, String encoding) {
        IPath p = ResourceAdapterFactory.create(new Path(path));
        IPath f = ResourceAdapterFactory.create(new Path(project.getName()
            + "/" + path));

        IFile fileMock = EasyMock.createMock(IFile.class);

        EasyMock.expect(fileMock.getProject()).andStubReturn(project);
        EasyMock.expect(fileMock.getProjectRelativePath()).andStubReturn(p);
        EasyMock.expect(fileMock.isDerived()).andStubReturn(false);
        EasyMock.expect(fileMock.exists()).andStubReturn(true);
        EasyMock.expect(fileMock.getType()).andStubReturn(IResource.FILE);
        EasyMock.expect(fileMock.getName()).andStubReturn(p.lastSegment());
        EasyMock.expect(fileMock.getFullPath()).andStubReturn(f);

        try {
            EasyMock.expect(fileMock.getContents()).andStubReturn(
                new ResetingInputStream(content));
            EasyMock.expect(fileMock.getCharset()).andStubReturn(encoding);
        } catch (IOException e) {
            // cannot happen as the mock is in recording mode
        }

        EasyMock.replay(fileMock);
        return fileMock;
    }

    protected IFolder createFolderMock(String path) {
        IPath p = ResourceAdapterFactory.create(new Path(path));
        IPath f = ResourceAdapterFactory.create(new Path(project.getName()
            + "/" + path));

        IFolder folderMock = EasyMock.createMock(IFolder.class);

        EasyMock.expect(folderMock.getProject()).andStubReturn(project);
        EasyMock.expect(folderMock.getProjectRelativePath()).andStubReturn(p);
        EasyMock.expect(folderMock.isDerived()).andStubReturn(false);
        EasyMock.expect(folderMock.exists()).andStubReturn(true);
        EasyMock.expect(folderMock.getType()).andStubReturn(IResource.FOLDER);
        EasyMock.expect(folderMock.getName()).andStubReturn(p.lastSegment());
        EasyMock.expect(folderMock.getFullPath()).andStubReturn(f);

        try {
            EasyMock.expect(folderMock.members()).andStubReturn(
                new IResource[0]);
        } catch (IOException e) {
            // cannot happen as the mock is in recording mode
        }

        EasyMock.replay(folderMock);
        return folderMock;
    }

    protected void assertPaths(List<String> actual, String... expected) {
        for (int i = 0; i < expected.length; i++) {
            assertTrue("Expected " + expected[i] + " to appear in: " + actual,
                actual.contains(expected[i]));
        }

        assertEquals(Arrays.toString(expected) + " != " + actual,
            expected.length, actual.size());
    }

}
