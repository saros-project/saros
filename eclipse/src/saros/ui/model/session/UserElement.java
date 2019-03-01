package saros.ui.model.session;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import saros.awareness.AwarenessInformationCollector;
import saros.editor.EditorManager;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.TreeContentProvider;
import saros.ui.model.TreeElement;
import saros.ui.util.ModelFormatUtils;
import saros.ui.util.SWTBoldStyler;

/**
 * Model element for a session {@link User user}. This element can only be used in conjunction with
 * a {@link TreeContentProvider}.
 */
public final class UserElement extends TreeElement {

  private final EditorManager editorManager;

  private final AwarenessInformationCollector collector;

  private final User user;

  /**
   * Child elements of this user element which may hold additional information such as the currently
   * opened file etc.
   */
  private final List<AwarenessInformationTreeElement> children =
      new ArrayList<AwarenessInformationTreeElement>();

  public UserElement(
      final User user,
      final EditorManager editorManager,
      final AwarenessInformationCollector collector) {
    if (user == null) throw new IllegalArgumentException("user is null");

    this.user = user;
    this.editorManager = editorManager;
    this.collector = collector;

    addAwarenessDetails();
  }

  /**
   * Returns the {@linkplain User user} that this user element represents.
   *
   * @return the user, never <code>null</code>
   */
  public final User getUser() {
    return user;
  }

  @Override
  public Object[] getChildren() {
    return children.toArray();
  }

  @Override
  public boolean hasChildren() {
    return children.size() != 0;
  }

  @Override
  public StyledString getStyledText() {
    StyledString text = new StyledString();

    /*
     * Blank space in the front for the highlighting color square, see
     * saros.ui.widgets.viewer.session.UserElementDecorator
     *
     * ColorSquare [HOST] Alice [following]
     */
    text.append("    ");

    if (user.isHost()) {
      text.append(Messages.UserElement_host, StyledString.COUNTER_STYLER);
    }

    text.append(ModelFormatUtils.getDisplayName(user));

    /*
     * Right level
     */
    if (user.hasReadOnlyAccess()) {
      text.append(" ");
      text.append(Messages.UserElement_read_only, StyledString.COUNTER_STYLER);
    }

    /*
     * Follow Mode: Who am I following? If this equals the user element we
     * are looking at, append the follow information to the user. Don't
     * append this info for any other users, because they have a
     * FollowModeTreeElement for this.
     */
    User followee = editorManager.getFollowedUser();

    if (!user.equals(followee)) return text;

    final String followModeState =
        collector.isActiveEditorShared(user)
            ? Messages.UserElement_following
            : Messages.UserElement_following_paused;

    text.append(" ");
    text.append(followModeState, SWTBoldStyler.STYLER);

    return text;
  }

  @Override
  public Image getImage() {
    return user.hasWriteAccess()
        ? ImageManager.ICON_CONTACT_SAROS_SUPPORT
        : ImageManager.ICON_USER_SAROS_READONLY;
  }

  private void addAwarenessDetails() {
    /*
     * remove this check if you want to display awareness information about
     * the local user to himself
     */
    if (user.isLocal()) return;

    children.add(new AwarenessInformationTreeElement(user, editorManager, collector));

    if (collector.getFollowedUser(user) != null) {
      children.add(new FollowModeInformationTreeElement(user, editorManager, collector));
    }
  }

  @Override
  public int hashCode() {
    return user.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof UserElement)) return false;

    UserElement other = (UserElement) obj;
    return user.equals(other.user);
  }

  public int compareTo(final UserElement other) {
    final User user1 = this.user;
    final User user2 = other.user;

    if (user1.equals(user2)) return 0;

    if (user1.isHost()) return -1;

    if (user2.isHost()) return +1;

    String nickname1 = ModelFormatUtils.getDisplayName(user1);
    String nickname2 = ModelFormatUtils.getDisplayName(user2);

    return nickname1.compareToIgnoreCase(nickname2);
  }
}
