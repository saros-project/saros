package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.session.User;
import java.util.List;
import org.apache.log4j.Logger;

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

  protected IReferencePoint referencePoint;
  /**
   * constructor to init doc.
   *
   * @param initState start document state.
   * @param referencePoint
   */
  public Document(String initState, IReferencePoint referencePoint, IPath path) {
    doc = new StringBuffer(initState);
    this.referencePoint = referencePoint;
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

    List<TextEditActivity> activities = op.toTextEdit(new SPath(referencePoint, path), dummy);

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
