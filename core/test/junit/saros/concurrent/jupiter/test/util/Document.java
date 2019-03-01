package saros.concurrent.jupiter.test.util;

import java.util.List;
import org.apache.log4j.Logger;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.session.User;

/**
 * this class represent a document object for testing.
 *
 * @author troll
 * @author oezbek
 */
public class Document {

  /**
   * Listener for jupiter document actions.
   *
   * @author orieger
   */
  public interface JupiterDocumentListener {

    public void documentAction(User user);

    public String getUser();
  }

  private static final Logger log = Logger.getLogger(Document.class.getName());

  /** document state. */
  protected StringBuffer doc;

  protected IPath path;

  protected IProject project;

  /**
   * constructor to init doc.
   *
   * @param initState start document state.
   */
  public Document(String initState, IProject project, IPath path) {
    doc = new StringBuffer(initState);
    this.project = project;
    this.path = path;
  }

  /**
   * return string representation of current doc state.
   *
   * @return string of current doc state.
   */
  public String getDocument() {
    return doc.toString();
  }

  @Override
  public String toString() {
    return doc.toString();
  }

  /**
   * Execute Operation on document state.
   *
   * @param op
   */
  public void execOperation(Operation op) {
    User dummy = JupiterTestCase.createUser("dummy");

    List<TextEditActivity> activities = op.toTextEdit(new SPath(project, path), dummy);

    for (TextEditActivity activity : activities) {

      int start = activity.getOffset();
      int end = start + activity.getReplacedText().length();
      String is = doc.toString().substring(start, end);

      if (!is.equals(activity.getReplacedText())) {
        log.warn("Text should be '" + activity.getReplacedText() + "' is '" + is + "'");
        throw new RuntimeException(
            "Text should be '" + activity.getReplacedText() + "' is '" + is + "'");
      }

      doc.replace(start, end, activity.getText());
    }
  }
}
