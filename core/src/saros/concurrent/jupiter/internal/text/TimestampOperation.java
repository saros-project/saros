package saros.concurrent.jupiter.internal.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Collections;
import java.util.List;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.session.User;

/**
 * This operation contains a new vector time for the algorithm.
 *
 * <p>TODO TimestampOperations are never used.
 *
 * @author orieger
 */
@XStreamAlias("timestampOp")
public class TimestampOperation implements Operation {

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Timestamp(0,'')";
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj.getClass().equals(getClass())) {
      return true;
    } else {
      return false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    int hashcode = 38;
    return hashcode;
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {
    return Collections.emptyList();
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    return Collections.emptyList();
  }

  /** {@inheritDoc} */
  @Override
  public Operation invert() {
    return new NoOperation(); // TimestampOperations don't cause effects
  }
}
