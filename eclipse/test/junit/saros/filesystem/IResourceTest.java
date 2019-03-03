package saros.filesystem;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class IResourceTest {

  @Test
  public void testAdaptTo() {
    org.eclipse.core.runtime.IPath path = EasyMock.createMock(org.eclipse.core.runtime.IPath.class);

    EasyMock.replay(path);

    org.eclipse.core.resources.IProject project =
        EasyMock.createMock(org.eclipse.core.resources.IProject.class);

    EasyMock.expect(project.getFullPath()).andStubReturn(path);

    EasyMock.replay(project);

    Capture<Class<Object>> mappedAdapterClassCapture = Capture.newInstance();

    org.eclipse.core.resources.IFolder folder =
        EasyMock.createMock(org.eclipse.core.resources.IFolder.class);

    EasyMock.expect(folder.getAdapter(EasyMock.capture(mappedAdapterClassCapture)))
        .andStubReturn(folder);

    EasyMock.expect(folder.getType()).andStubReturn(org.eclipse.core.resources.IResource.FOLDER);

    EasyMock.expect(folder.getProject()).andStubReturn(project);

    EasyMock.replay(folder);

    final IFolder coreFolder = new EclipseFolderImpl(folder);

    final Object adapted = coreFolder.adaptTo(IFolder.class);

    EasyMock.verify(folder);

    Assert.assertEquals(
        org.eclipse.core.resources.IFolder.class, mappedAdapterClassCapture.getValue());

    Assert.assertTrue("returned adapter is not a folder", adapted instanceof IFolder);
  }
}
