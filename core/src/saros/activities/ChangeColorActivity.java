package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Activity for managing color changes.
 *
 * @author cnk
 * @author tobi
 * @author Stefan Rossbach
 */
@XStreamAlias("changeColorActivity")
public class ChangeColorActivity extends AbstractActivity implements ITargetedActivity {

  @XStreamAsAttribute protected final User target;

  @XStreamAsAttribute protected final User affected;

  @XStreamAsAttribute protected final int colorID;

  public ChangeColorActivity(User source, User target, User affected, int colorID) {

    super(source);

    if (target == null) throw new IllegalArgumentException("target must not be null");
    if (affected == null) throw new IllegalArgumentException("affected must not be null");

    this.target = target;
    this.affected = affected;
    this.colorID = colorID;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (target != null) && (affected != null);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public String toString() {
    return "ChangeColorActivity(source: "
        + getSource()
        + ", affected: "
        + affected
        + ", colorID: "
        + colorID
        + ")";
  }

  /**
   * Returns the user that color id should be changed
   *
   * @return the affected user or <code>null</code> if the user is no longer part of the session.
   *     <br>
   *     <i>TODO Cannot be null, since field is final and ctor prevents null value</i>
   */
  public User getAffected() {
    return affected;
  }

  /**
   * Returns the new color id for the affected user.
   *
   * @return the new color id
   */
  public int getColorID() {
    return colorID;
  }

  @Override
  public User getTarget() {
    return target;
  }
}
