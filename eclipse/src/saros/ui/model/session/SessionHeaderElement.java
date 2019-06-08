package saros.ui.model.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import saros.awareness.AwarenessInformationCollector;
import saros.editor.EditorManager;
import saros.session.User;
import saros.session.internal.SarosSession;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.HeaderElement;
import saros.ui.model.TreeElement;

/**
 * Container {@link TreeElement} for a {@link SarosSession}
 *
 * @author bkahlert
 */
public class SessionHeaderElement extends HeaderElement {
  private final SessionInput sessionInput;
  private final EditorManager editorManager;
  private final AwarenessInformationCollector collector;

  public SessionHeaderElement(
      final Font font,
      final SessionInput sessionInput,
      final EditorManager editorManager,
      AwarenessInformationCollector collector) {

    super(font);
    this.sessionInput = sessionInput;
    this.editorManager = editorManager;
    this.collector = collector;
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString();
    if (sessionInput == null || sessionInput.getSession() == null) {
      styledString.append(Messages.SessionHeaderElement_no_session_running, boldStyler);
    } else {
      styledString.append(Messages.SessionHeaderElement_session, boldStyler);
    }
    return styledString;
  }

  @Override
  public Image getImage() {
    return ImageManager.ELCL_SESSION;
  }

  @Override
  public boolean hasChildren() {
    return sessionInput != null && sessionInput.getSession() != null;
  }

  @Override
  public Object[] getChildren() {

    if (sessionInput == null || sessionInput.getSession() == null) return new Object[0];

    final List<UserElement> userElements = new ArrayList<UserElement>();

    final List<User> users = sessionInput.getSession().getUsers();

    for (final User user : users) userElements.add(new UserElement(user, editorManager, collector));

    return userElements.toArray();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sessionInput);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    SessionHeaderElement other = (SessionHeaderElement) obj;
    return Objects.equals(sessionInput, other.sessionInput);
  }
}
