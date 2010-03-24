package de.fu_berlin.inf.dpp.util.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

public class JIDConverter extends AbstractSingleValueConverter {

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return type.equals(JID.class);
    }

    @Override
    public Object fromString(String str) {
        return new JID(Util.urlUnescape(str));
    }

    @Override
    public String toString(Object obj) {
        return Util.urlEscape(((JID) obj).toString());
    }

}
