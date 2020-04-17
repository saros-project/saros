package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Objects;
import saros.session.User;

public abstract class AbstractResourceActivity extends AbstractActivity
    implements IResourceActivity {

  @XStreamAlias("p")
  private final SPath path;

  public AbstractResourceActivity(User source, SPath path) {
    super(source);

    this.path = path;
  }

  @Override
  public boolean isValid() {
    /*
     * TODO path must never be null for IResourceActivities. Add a
     * StatusActivity for informing remote users that no shared resource is
     * active anymore.
     */
    return super.isValid() /* && (path != null) */;
  }

  // TODO make protected or remove
  @Override
  @Deprecated
  public SPath getPath() {
    return path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hashCode(path);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof AbstractResourceActivity)) return false;

    AbstractResourceActivity other = (AbstractResourceActivity) obj;

    if (!Objects.equals(this.path, other.path)) return false;

    return true;
  }
}
