package saros.resource_change_handlers;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.filesystem.EclipseFile;
import saros.net.xmpp.JID;
import saros.session.User;

public class FileActivityConsumerTest {

  private static final Charset charset = StandardCharsets.UTF_8;

  private static final byte[] FILE_CONTENT = "abc".getBytes(charset);

  private static final byte[] INCOMING_SAME_CONTENT = FILE_CONTENT.clone();
  private static final byte[] INCOMING_DIFFERENT_CONTENT = "abd".getBytes(charset);

  /** Unit under test */
  private FileActivityConsumer consumer;

  /** Partial file mock in recording state, has to be replayed in every test before being used. */
  private IFile file;

  /** Mocked saros file wrapper to test. */
  private EclipseFile eclipseFileWrapper;

  private SharedResourcesManager resourceChangeListener;

  @Before
  public void setUp() throws CoreException {

    // set up resource change listener mock
    resourceChangeListener = createMock(SharedResourcesManager.class);

    resourceChangeListener.suspend();
    expectLastCall().once();

    resourceChangeListener.resume();
    expectLastCall().once();

    replay(resourceChangeListener);

    consumer = new FileActivityConsumer(null, resourceChangeListener, null);

    // set up eclipse resource mock
    file = createMock(IFile.class);
    eclipseFileWrapper = createNiceMock(EclipseFile.class);

    expect(file.getContents()).andStubReturn(new ByteArrayInputStream(FILE_CONTENT));

    expect(file.exists()).andStubReturn(Boolean.TRUE);
    expect(file.getType()).andStubReturn(org.eclipse.core.resources.IResource.FILE);
    expect(file.getAdapter(IFile.class)).andStubReturn(file);

    // set up saros resource wrapper mocks
    expect(eclipseFileWrapper.getDelegate()).andStubReturn(file);
    replay(eclipseFileWrapper);
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

    consumer.exec(createFileActivity(INCOMING_SAME_CONTENT));

    verify(file);
  }

  @Test
  public void testExecFileActivityCreationDifferentContent() throws CoreException {

    file.setContents(
        isA(InputStream.class), anyBoolean(), anyBoolean(), anyObject(IProgressMonitor.class));

    expectLastCall().once();

    replay(file);

    consumer.exec(createFileActivity(INCOMING_DIFFERENT_CONTENT));

    // ensure file was written
    verify(file);
  }

  private FileActivity createFileActivity(byte[] content) {
    return new FileActivity(
        new User(new JID("foo@bar"), true, true, null),
        Type.CREATED,
        Purpose.ACTIVITY,
        eclipseFileWrapper,
        null,
        content,
        charset.name());
  }
}
