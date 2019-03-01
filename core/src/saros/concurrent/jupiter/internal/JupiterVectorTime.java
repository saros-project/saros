package saros.concurrent.jupiter.internal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.concurrent.jupiter.Timestamp;
import saros.concurrent.jupiter.VectorTime;

/** This class models the vector time for the Jupiter control algorithm. */
@XStreamAlias("vectorTime")
public class JupiterVectorTime implements VectorTime {

  /** Counter for the number of local operations. */
  @XStreamAlias("local")
  @XStreamAsAttribute
  private int localOperationCount;

  /** Counter for the number of remote operations. */
  @XStreamAlias("remote")
  @XStreamAsAttribute
  private int remoteOperationCount;

  /**
   * Create a new JupiterVectorTime.
   *
   * @param localCount the local operation count.
   * @param remoteCount the remote operation count.
   */
  public JupiterVectorTime(int localCount, int remoteCount) {
    if (localCount < 0) {
      throw new IllegalArgumentException("local operation count cannot be negative");
    }
    if (remoteCount < 0) {
      throw new IllegalArgumentException("remote operation count cannot be negative");
    }
    this.localOperationCount = localCount;
    this.remoteOperationCount = remoteCount;
  }

  /** @see VectorTime#getAt(int) */
  @Override
  public int getAt(int index) {
    if (index == 0) {
      return getLocalOperationCount();
    } else if (index == 1) {
      return getRemoteOperationCount();
    } else {
      throw new IndexOutOfBoundsException("" + index);
    }
  }

  /** @see VectorTime#getLength() */
  @Override
  public int getLength() {
    return 2;
  }

  /** @see Timestamp#getComponents() */
  @Override
  public int[] getComponents() {
    return new int[] {getLocalOperationCount(), getRemoteOperationCount()};
  }

  /** @return Returns the local operation count. */
  public int getLocalOperationCount() {
    return this.localOperationCount;
  }

  /** @return Returns the remote operation count. */
  public int getRemoteOperationCount() {
    return this.remoteOperationCount;
  }

  /**
   * Increment the local operation counter.
   *
   * @return the counter after increment.
   */
  public JupiterVectorTime incrementLocalOperationCount() {
    return new JupiterVectorTime(localOperationCount + 1, remoteOperationCount);
  }

  /**
   * Increment the remote operation counter.
   *
   * @return the counter after increment.
   */
  public JupiterVectorTime incrementRemoteOperationCount() {
    return new JupiterVectorTime(localOperationCount, remoteOperationCount + 1);
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[");
    buffer.append(this.localOperationCount);
    buffer.append(",");
    buffer.append(this.remoteOperationCount);
    buffer.append("]");
    return buffer.toString();
  }

  /** @see java.lang.Object#equals(java.lang.Object) */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj.getClass().equals(getClass())) {
      JupiterVectorTime vector = (JupiterVectorTime) obj;
      return (vector.localOperationCount == this.localOperationCount)
          && (vector.remoteOperationCount == this.remoteOperationCount);
    } else {
      return false;
    }
  }

  /** @see java.lang.Object#hashCode() */
  @Override
  public int hashCode() {
    int hashcode = 17;
    hashcode = 37 * hashcode + this.localOperationCount;
    hashcode = 37 * hashcode + this.remoteOperationCount;
    return hashcode;
  }
}
