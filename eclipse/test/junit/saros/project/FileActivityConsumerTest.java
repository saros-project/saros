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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.filesystem.EclipseReferencePointManager;
import saros.filesystem.IReferencePoint;
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

  private IProject project;
  private IPath path;
  private IReferencePoint referencePoint;

  private SharedResourcesManager resourceChangeListener;
  private EclipseReferencePointManager eclipseReferencePointManager;

  @Before
  public void setUp() throws CoreException {

    resourceChangeListener = createMock(SharedResourcesManager.class);

    resourceChangeListener.suspend();
    expectLastCall().once();

    resourceChangeListener.resume();
    expectLastCall().once();

    replay(resourceChangeListener);

    referencePoint = createMock(IReferencePoint.class);

    replay(referencePoint);

    path = createMock(IPath.class);
    replay(path);

    project = createMock(IProject.class);
    replay(project);

    file = createMock(IFile.class);

    expect(file.getContents()).andStubReturn(new ByteArrayInputStream(FILE_CONTENT));

    expect(file.exists()).andStubReturn(Boolean.TRUE);
    expect(file.getType()).andStubReturn(IResource.FILE);
    expect(file.getAdapter(IFile.class)).andStubReturn(file);
    expect(file.getProject()).andStubReturn(project);
    expect(file.getProjectRelativePath()).andStubReturn(path);
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

    IActivity activity = createFileActivity(file, INCOMING_SAME_CONTENT);
    consumer.exec(activity);

    verify(file);
  }

  @Test
  public void testExecFileActivityCreationDifferentContent() throws CoreException {

    file.setContents(
        isA(InputStream.class), anyBoolean(), anyBoolean(), anyObject(IProgressMonitor.class));

    expectLastCall().once();

    replay(file);

    IActivity activity = createFileActivity(file, INCOMING_DIFFERENT_CONTENT);

    consumer.exec(activity);

    // ensure file was written
    verify(file);
  }

  private SPath createPathMockForFile(IFile file) {
    final SPath path = createMock(SPath.class);

    expect(path.getReferencePoint()).andStubReturn(referencePoint);
    expect(path.getProjectRelativePath()).andStubReturn(ResourceAdapterFactory.create(this.path));
    replay(path);

    eclipseReferencePointManager = createMock(EclipseReferencePointManager.class);
    expect(eclipseReferencePointManager.getProject(referencePoint)).andStubReturn(project);
    expect(eclipseReferencePointManager.getFile(path)).andStubReturn(file);

    replay(eclipseReferencePointManager);

    consumer =
        new FileActivityConsumer(null, resourceChangeListener, null, eclipseReferencePointManager);

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
