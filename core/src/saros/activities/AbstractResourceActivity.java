package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Objects;
import saros.filesystem.IResource;
import saros.session.User;

// TODO enforce resource!=null when EditorActivity is fixed
public abstract class AbstractResourceActivity<T extends IResource> extends AbstractActivity
    implements IResourceActivity<T> {

  @XStreamAlias("r")
  private final ResourceTransportWrapper<T> resourceTransportWrapper;

  public AbstractResourceActivity(User source, T resource) {
    super(source);

    this.resourceTransportWrapper =
        resource != null ? new ResourceTransportWrapper<>(resource) : null;
  }

  @Override
  public boolean isValid() {
    /*
     * TODO file must never be null for IResourceActivities. Add a
     * StatusActivity for informing remote users that no shared resource is
     * active anymore.
     */
    return super.isValid();
  }

  public T getResource() {
    return resourceTransportWrapper != null ? resourceTransportWrapper.getResource() : null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hashCode(resourceTransportWrapper);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof AbstractResourceActivity)) return false;

    AbstractResourceActivity<?> other = (AbstractResourceActivity<?>) obj;

    return Objects.equals(this.resourceTransportWrapper, other.resourceTransportWrapper);
  }
}
