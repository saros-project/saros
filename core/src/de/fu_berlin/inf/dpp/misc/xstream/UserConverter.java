package de.fu_berlin.inf.dpp.misc.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.Startable;

/**
 * Converts session-dependent User objects to session-independent XML representations, and vice
 * versa.
 */
@Component
public class UserConverter extends AbstractSingleValueConverter implements Startable {

  private ISarosSession session;

  public UserConverter(ISarosSession session) {
    this.session = session;
  }

  @Override
  public void start() {
    ActivitiesExtension.PROVIDER.registerConverter(this);
  }

  @Override
  public void stop() {
    ActivitiesExtension.PROVIDER.unregisterConverter(this);
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(User.class);
  }

  @Override
  public String toString(Object obj) {
    JID jid = ((User) obj).getJID();
    return URLCodec.encode(jid.toString());
  }

  @Override
  public Object fromString(String str) {
    JID jid = new JID(URLCodec.decode(str));
    return session.getUser(jid);
  }
}
