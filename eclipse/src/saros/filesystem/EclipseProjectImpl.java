package saros.filesystem;

/** @deprecated use {@link EclipseReferencePointImpl} instead */
@Deprecated
public class EclipseProjectImpl extends EclipseContainerImpl implements IReferencePoint {

  EclipseProjectImpl(org.eclipse.core.resources.IProject delegate) {
    super(delegate);
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IProject IProject} object.
   *
   * @return
   */
  @Override
  public org.eclipse.core.resources.IProject getDelegate() {
    return (org.eclipse.core.resources.IProject) delegate;
  }
}
