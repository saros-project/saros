package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Objects;
import saros.filesystem.IResource;
import saros.misc.xstream.ResourceTransportWrapperConverter;

/**
 * A wrapper for <code>IResource</code> objects.
 *
 * <p>The wrapper offers the capabilities to marshal and unmarshal the contained resource for
 * transport.
 *
 * @param <T> the type of the contained resource
 * @see ResourceTransportWrapperConverter
 */
@XStreamAlias("RTW")
public class ResourceTransportWrapper<T extends IResource> {

  private final T resource;

  public ResourceTransportWrapper(T resource) {
    Objects.requireNonNull(resource, "The given resource must not be null!");

    this.resource = resource;
  }

  public T getResource() {
    return resource;
  }

  @Override
  public int hashCode() {
    return resource.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (!(obj instanceof ResourceTransportWrapper)) return false;

    ResourceTransportWrapper<?> other = (ResourceTransportWrapper<?>) obj;

    return Objects.equals(resource, other.resource);
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + " - " + resource + "]";
  }
}
