package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.ObjectUtils;
import saros.session.User;

public abstract class AbstractActivity implements IActivity {

  @XStreamAsAttribute private final User source;

  /**
   * @JTourBusStop 2, Creating a new Activity type, The abstract class to extend:
   *
   * <p>Instead of directly implementing IActivity, any new activity type should extends this class.
   *
   * <p>However, there is an important subtype of activities: An activity that refers to a resource,
   * such as a file, should implement the more specialized IResourceActivity interface. And guess
   * what, there is an abstract class for this interface, too: AbstractResourceActivity.
   *
   * <p>So once you decided which of these two abstract classes to extend, you can provide your new
   * class with all fields and methods you deem necessary, and then continue with the next stop.
   */

  /** @param source Must not be <code>null</code> */
  public AbstractActivity(User source) {
    if (source == null) throw new IllegalArgumentException("Source cannot be null");

    this.source = source;
  }

  @Override
  public boolean isValid() {
    return source != null;
  }

  @Override
  public User getSource() {
    return this.source;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(source);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof AbstractActivity)) return false;

    AbstractActivity other = (AbstractActivity) obj;

    if (!ObjectUtils.equals(this.source, other.source)) return false;

    return true;
  }
}
