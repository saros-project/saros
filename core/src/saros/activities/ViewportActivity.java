package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.session.User;

@XStreamAlias("viewportActivity")
public class ViewportActivity extends AbstractResourceActivity {

  @XStreamAlias("o")
  @XStreamAsAttribute
  protected final int startLine;

  @XStreamAlias("l")
  @XStreamAsAttribute
  protected final int numberOfLines;

  public ViewportActivity(User source, int startLine, int numberOfLines, SPath path) {

    super(source, path);

    if (path == null) throw new IllegalArgumentException("path must not be null");

    this.startLine = Math.max(0, startLine);
    this.numberOfLines = Math.max(0, numberOfLines);
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getPath() != null);
  }

  /**
   * Returns the number of lines that this viewport activity uses. Note: getStartLine() +
   * getNumberOfLines = first line <b>after</b> viewport range described by this activity.
   *
   * @return
   */
  public int getNumberOfLines() {
    return this.numberOfLines;
  }

  /**
   * Returns the start line of this viewport activity.
   *
   * @return
   */
  public int getStartLine() {
    return this.startLine;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + startLine;
    result = prime * result + numberOfLines;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof ViewportActivity)) return false;

    ViewportActivity other = (ViewportActivity) obj;

    if (this.startLine != other.startLine) return false;
    if (this.numberOfLines != other.numberOfLines) return false;

    return true;
  }

  @Override
  public String toString() {
    return "ViewportActivity(path: "
        + getPath()
        + ", range: ("
        + startLine
        + ","
        + (startLine + numberOfLines)
        + "))";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
