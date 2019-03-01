package saros.project;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.SPath;
import saros.filesystem.IResource;
import saros.filesystem.ResourceAdapterFactory;
import saros.net.xmpp.JID;
import saros.session.User;

public class FileActivityConsumerTest {

  private static final byte[] FILE_CONTENT = new byte[] {'a', 'b', 'c'};

  private static final byte[] INCOMING_SAME_CONTENT = FILE_CONTENT.clone();
  private static final byte[] INCOMING_DIFFERENT_CONTENT = new byte[] {'a', 'b', 'd'};

  /** Unit under test */
  private FileActivityConsumer consumer;

  /** Partial file mock in recording state, has to be replayed in every test before being used. */
  private IFile file;

  private SharedResourcesManager resourceChangeListener;

  @Before
  public void setUp() throws CoreException {

    resourceChangeListener = createMock(SharedResourcesManager.class);

    resourceChangeListener.suspend();
    expectLastCall().once();

    resourceChangeListener.resume();
    expectLastCall().once();

    replay(resourceChangeListener);

    consumer = new FileActivityConsumer(null, resourceChangeListener, null);

    file = createMock(IFile.class);

    expect(file.getContents()).andStubReturn(new ByteArrayInputStream(FILE_CONTENT));

    expect(file.exists()).andStubReturn(Boolean.TRUE);
    expect(file.getType()).andStubReturn(IResource.FILE);
    expect(file.getAdapter(IFile.class)).andStubReturn(file);
  }

  @After
  public void tearDown() {
    verify(resourceChangeListener);
  }

  @Test
  public void testExecFileActivityCreationSameContent() throws CoreException {

    file.setContents(
        isA(InputStream.class), anyBoolean(), anyBoolean(), anyObject(IProgressMonitor.class));

    expectLastCall().andStubThrow(new AssertionError("file was written unnecessarily"));

    replay(file);

    consumer.exec(createFileActivity(file, INCOMING_SAME_CONTENT));

    verify(file);
  }

  @Test
  public void testExecFileActivityCreationDifferentContent() throws CoreException {

    file.setContents(
        isA(InputStream.class), anyBoolean(), anyBoolean(), anyObject(IProgressMonitor.class));

    expectLastCall().once();

    replay(file);

    consumer.exec(createFileActivity(file, INCOMING_DIFFERENT_CONTENT));

    // ensure file was written
    verify(file);
  }

  private SPath createPathMockForFile(IFile file) {
    final SPath path = createMock(SPath.class);

    expect(path.getFile()).andStubReturn(ResourceAdapterFactory.create(file));

    replay(path);

    return path;
  }

  private FileActivity createFileActivity(IFile file, byte[] content) {
    return new FileActivity(
        new User(new JID("foo@bar"), true, true, 0, 0),
        Type.CREATED,
        Purpose.ACTIVITY,
        createPathMockForFile(file),
        null,
        content,
        null);
  }
}
