package saros.ui.model.session;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import saros.awareness.AwarenessInformationCollector;
import saros.editor.EditorManager;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.ModelFormatUtils;

/**
 * This is a tree element that can be displayed as a child element of the user entry in the Saros
 * session view {@link Viewer Viewers} showing information about the state of that user / his past
 * actions or whatever awareness information might help to be more productive in a session.
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class FollowModeInformationTreeElement extends AwarenessInformationTreeElement {

  public FollowModeInformationTreeElement(
      final User user,
      final EditorManager editorManager,
      final AwarenessInformationCollector collector) {
    super(user, editorManager, collector);
  }

  /**
   * Combines the available awareness information to a styled string
   *
   * <p>TODO: (optional) create a new renderer that presents the information in a more user friendly
   * way, not just text
   */
  @Override
  public StyledString getStyledText() {

    StyledString styledString = new StyledString();
    final String following_paused = Messages.UserElement_following_paused;

    User followee = collector.getFollowedUser(user);
    if (followee != null) {
      if (collector.isActiveEditorShared(followee)) {
        styledString.append("following " + ModelFormatUtils.getDisplayName(followee));
      } else {
        styledString.append(following_paused);
      }
    } else {
      styledString.append("Not following anyone");
    }
    return styledString;
  }

  /**
   * Display an appropriate image for this element depending on the awareness information that is
   * currently shown.
   *
   * <p>At the moment this is only a "file object" icon in case the user has a file opened.
   *
   * <p>TODO: set icons properly depending on the state of the user/his actions.
   */
  @Override
  public Image getImage() {
    return ImageManager.ICON_USER_SAROS_FOLLOWMODE;
  }
}
