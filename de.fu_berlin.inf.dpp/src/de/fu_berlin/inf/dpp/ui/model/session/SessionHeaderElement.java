package de.fu_berlin.inf.dpp.ui.model.session;

import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

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
    return ObjectUtils.hashCode(sessionInput);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    SessionHeaderElement other = (SessionHeaderElement) obj;
    return ObjectUtils.equals(sessionInput, other.sessionInput);
  }
}
