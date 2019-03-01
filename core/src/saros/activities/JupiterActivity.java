package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.ObjectUtils;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.Timestamp;
import saros.session.User;

/** A JupiterActivity is an Activity that can be handled by the Jupiter Algorithm. */
@XStreamAlias("jupiterActivity")
public class JupiterActivity extends AbstractResourceActivity {

  /** Timestamp that specifies the definition context of the enclosed operation. */
  @XStreamAlias("t")
  private final Timestamp timestamp;

  @XStreamAlias("o")
  private final Operation operation;

  public JupiterActivity(Timestamp timestamp, Operation operation, User source, SPath path) {

    super(source, path);

    this.timestamp = timestamp;
    this.operation = operation;
  }

  public Operation getOperation() {
    return this.operation;
  }

  public Timestamp getTimestamp() {
    return this.timestamp;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof JupiterActivity)) return false;

    JupiterActivity other = (JupiterActivity) obj;

    if (!ObjectUtils.equals(this.operation, other.operation)) return false;
    if (!ObjectUtils.equals(this.timestamp, other.timestamp)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ObjectUtils.hashCode(operation);
    result = prime * result + ObjectUtils.hashCode(timestamp);
    return result;
  }

  @Override
  public String toString() {
    return "JupiterActivity(timestamp: "
        + timestamp
        + ", operation: "
        + operation
        + ", source: "
        + getSource()
        + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
