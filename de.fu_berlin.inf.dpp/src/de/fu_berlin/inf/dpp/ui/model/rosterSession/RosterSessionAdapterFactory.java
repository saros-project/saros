package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;

/**
 * Provides adapters for {@link SarosSession} entities which are provided by
 * {@link RosterSessionContentProvider}.
 * <p>
 * E.g. let's you adapt {@link UserElement} to {@link User} with
 * 
 * <pre>
 * User user = (User) userElement.getAdapter(User.class);
 * if (user != null)
 *     return true;
 * else
 *     return false;
 * </pre>
 * <p>
 * <b>IMPORTANT:</b> If you update this class, please also update the extension
 * <code>org.eclipse.core.runtime.adapters</code> in <code>plugin.xml</code>!<br/>
 * Eclipse needs to know which object can be adapted to which type.
 * 
 * @author bkahlert
 */
public class RosterSessionAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return new Class[] { User.class };
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof UserElement) {
            if (adapterType == User.class) {
                return ((UserElement) adaptableObject).getUser();
            }
        }

        return null;
    }

}
