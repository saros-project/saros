package de.fu_berlin.inf.dpp.project;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.FileUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class SharedResourcesManagerTest {

    private static final byte[] FILE_CONTENT = new byte[] { 'a', 'b', 'c' };
    private static final byte[] INCOMING_SAME_CONTENT = FILE_CONTENT;
    private static final byte[] INCOMING_DIFFERENT_CONTENT = new byte[] { 'a',
        'b', 'd' };

    /** Unit under test */
    private static SharedResourcesManager manager;

    /** Reusable path for FileAcitivities */
    private static SPath path;

    /** Was FileUtils.writeFile() called? */
    private static boolean calledWriteFile = false;

    @BeforeClass
    public static void setup() throws CoreException {
        manager = new SharedResourcesManager(null, null);

        IFile file = EasyMock.createMock(IFile.class);

        /**
         * andStubReturn(new ByteArrayInputStream()) wouldn't work because the
         * Stream can only be read once (and needs to be resetted afterwards).
         * Instead, we provide a new Stream for every call (alternatively, these
         * mocks could be re-created in the @Before method).
         */
        expect(file.getContents()).andStubAnswer(new IAnswer<InputStream>() {
            @Override
            public InputStream answer() throws Throwable {
                return new ByteArrayInputStream(FILE_CONTENT);
            }
        });
        expect(file.exists()).andStubReturn(Boolean.TRUE);
        expect(file.getType()).andStubReturn(IResource.FILE);
        expect(file.getAdapter(IFile.class)).andStubReturn(file);
        replay(file);

        path = createMock(SPath.class);
        expect(path.getFile()).andStubReturn(
            ResourceAdapterFactory.create(file));
        replay(path);
    }

    @Before
    public void before() throws CoreException {
        mockStaticPartial(FileUtils.class, "writeFile");

        /**
         * We only care about <code>FileUtils.writeFile()</code>. When it's
         * called, we log it.
         */
        FileUtils.writeFile(isA(InputStream.class), isA(IFile.class),
            isA(IProgressMonitor.class));
        expectLastCall().andStubAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                calledWriteFile = true;
                return null;
            }
        });

        replayAll();

        calledWriteFile = false;
    }

    @Test
    public void testExecFileActivityCreationSameContent() throws CoreException {
        FileActivity fileActivity = fileCreationWithContent(INCOMING_SAME_CONTENT);

        manager.handleFileActivity(fileActivity);

        assertFalse("file was written unnecessarily", calledWriteFile);
    }

    @Test
    public void testExecFileActivityCreationDifferentContent()
        throws CoreException {
        FileActivity fileActivity = fileCreationWithContent(INCOMING_DIFFERENT_CONTENT);

        manager.handleFileActivity(fileActivity);

        assertTrue("file was not written although content was new",
            calledWriteFile);
    }

    private FileActivity fileCreationWithContent(byte[] content) {
        return new FileActivity(createNiceMock(User.class), Type.CREATED, path,
            null, content, null, Purpose.ACTIVITY);
    }
}
