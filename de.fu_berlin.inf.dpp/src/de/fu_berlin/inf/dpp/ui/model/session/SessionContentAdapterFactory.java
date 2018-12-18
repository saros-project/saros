package de.fu_berlin.inf.dpp.ui.model.session;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Provides adapters for {@link SarosSession} entities which are provided by {@link
 * SessionContentProvider}.
 *
 * <p>E.g. let's you adapt {@link UserElement} to {@link User} with
 *
 * <pre>
 * User user = (User) userElement.getAdapter(User.class);
 *
 * if (user != null)
 *     return true;
 * else
 *     return false;
 * </pre>
 *
 * <p><b>IMPORTANT:</b> If you update this class, please also update the extension <code>
 * org.eclipse.core.runtime.adapters</code> in <code>plugin.xml</code>!<br>
 * Eclipse needs to know which object can be adapted to which type.
 *
 * @author bkahlert
 */
public class SessionContentAdapterFactory implements IAdapterFactory {

  @Override
  @SuppressWarnings("rawtypes")
  public Class[] getAdapterList() {
    return new Class[] {User.class};
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adapterType != User.class) return null;

    if (adaptableObject instanceof UserElement) return ((UserElement) adaptableObject).getUser();

    if (adaptableObject instanceof AwarenessInformationTreeElement)
      return ((AwarenessInformationTreeElement) adaptableObject).getUser();

    return null;
  }
}
