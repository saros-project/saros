/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import saros.session.User;

/**
 * A Checksum Error is a notification sent to the host by a user who wants inconsistencies to be
 * recovered.
 *
 * <p>The host will reply with a ChecksumError of the same recoveryID after having sent the last
 * FileActivity (with {@link FileActivity#isRecovery()} being set related to this checksum recovery.
 */
@XStreamAlias("checksumErrorActivity")
public class ChecksumErrorActivity extends AbstractActivity implements ITargetedActivity {

  @XStreamAsAttribute private User target;

  @XStreamAsAttribute protected String recoveryID;

  @XStreamImplicit protected List<SPath> paths;

  public ChecksumErrorActivity(User source, User target, List<SPath> paths, String recoveryID) {

    super(source);

    if (target == null) throw new IllegalArgumentException("target must not be null");

    this.target = target;
    this.paths = paths;
    this.recoveryID = recoveryID;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (target != null);
  }

  public List<SPath> getPaths() {
    return paths;
  }

  /** Each ChecksumError has a unique ID, which should be used to identify a recovery session */
  public String getRecoveryID() {
    return recoveryID;
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ObjectUtils.hashCode(paths);
    result = prime * result + ObjectUtils.hashCode(recoveryID);
    result = prime * result + ObjectUtils.hashCode(target);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof ChecksumErrorActivity)) return false;

    ChecksumErrorActivity other = (ChecksumErrorActivity) obj;

    if (!ObjectUtils.equals(this.recoveryID, other.recoveryID)) return false;
    if (!ObjectUtils.equals(this.paths, other.paths)) return false;
    if (!ObjectUtils.equals(this.target, other.target)) return false;

    return true;
  }

  @Override
  public String toString() {
    return "ChecksumErrorActivity(src: "
        + getSource()
        + ", target: "
        + target
        + ", paths: "
        + paths
        + ", recoveryID: "
        + recoveryID
        + ")";
  }

  @Override
  public User getTarget() {
    return target;
  }
}
