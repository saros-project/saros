package de.fu_berlin.inf.dpp.ui.util;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.UserFormatUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/** Utility class for formatting model elements for display in the UI. */
public class ModelFormatUtils {

  private ModelFormatUtils() {
    // It's a utility class, no public ctor
  }

  /**
   * Retrieves a user's nickname from the XMPP roster. If none is present it returns the base name.
   *
   * @param user
   * @return the user's nickname, or if none is set JID's base.
   */
  public static String getDisplayName(User user) {
    return UserFormatUtils.getDisplayName(user);
  }

  /**
   * This method formats patterns for display in the UI by replacing occurrences of {@link User}
   * objects by {@link #getDisplayName(User)}.
   *
   * @param pattern
   * @param arguments occurrences of User objects are replaced by their display name
   * @return the formatted string
   */
  public static String format(String pattern, Object... arguments) {
    List<Object> mappedValues = new ArrayList<Object>(arguments.length);
    for (Object obj : arguments) {
      if (obj instanceof User) {
        User user = (User) obj;
        mappedValues.add(getDisplayName(user));
      } else {
        mappedValues.add(obj);
      }
    }
    return MessageFormat.format(pattern, mappedValues.toArray());
  }
}
