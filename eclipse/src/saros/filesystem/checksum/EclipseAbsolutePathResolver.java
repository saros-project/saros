package saros.filesystem.checksum;

import org.eclipse.core.runtime.IPath;
import saros.filesystem.IFile;
import saros.filesystem.ResourceConverter;

/**
 * Helper class returning the location of the given <code>IFile</code>.
 *
 * @see org.eclipse.core.resources.IFile#getLocation()
 */
public class EclipseAbsolutePathResolver implements IAbsolutePathResolver {

  @Override
  public String getAbsolutePath(IFile file) {
    org.eclipse.core.resources.IFile eclipseFile = ResourceConverter.getDelegate(file);

    IPath eclipsePath = eclipseFile.getLocation();

    if (eclipsePath == null) {
      return null;
    }

    return eclipsePath.toOSString();
  }
}
