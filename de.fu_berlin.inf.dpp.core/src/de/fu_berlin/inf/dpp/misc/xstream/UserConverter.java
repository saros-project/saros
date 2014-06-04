package de.fu_berlin.inf.dpp.misc.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Converts session-dependent User objects to session-independent XML
 * representations, and vice versa.
 */
public class UserConverter extends AbstractSingleValueConverter {

    private ISarosSession session;

    public UserConverter(ISarosSession session) {
        this.session = session;
    }

    @SuppressWarnings({ "rawtypes" })
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
