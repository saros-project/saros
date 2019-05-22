package saros.filesystem;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class IResourceTest {

  @Test
  public void testAdaptTo() {

    org.eclipse.core.resources.IFolder folder =
        EasyMock.createMock(org.eclipse.core.resources.IFolder.class);

    Capture<Class<Object>> mappedAdapterClassCapture = Capture.newInstance();

    EasyMock.expect(folder.getAdapter(EasyMock.capture(mappedAdapterClassCapture)))
        .andStubReturn(folder);

    EasyMock.expect(folder.getType()).andStubReturn(org.eclipse.core.resources.IResource.FOLDER);

    EasyMock.replay(folder);

    final IFolder coreFolder = new EclipseFolderImpl(folder);

    final Object adapted = coreFolder.adaptTo(IFolder.class);

    EasyMock.verify(folder);

    Assert.assertEquals(
        org.eclipse.core.resources.IFolder.class, mappedAdapterClassCapture.getValue());

    Assert.assertTrue("returned adapter is not a folder", adapted instanceof IFolder);
  }
}
