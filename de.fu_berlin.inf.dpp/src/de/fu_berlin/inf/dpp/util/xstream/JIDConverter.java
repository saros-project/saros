package de.fu_berlin.inf.dpp.util.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Utils;

public class JIDConverter extends AbstractSingleValueConverter {

    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean canConvert(Class type) {
        return type.equals(JID.class);
    }

    @Override
    public Object fromString(String str) {
        return new JID(Utils.urlUnescape(str));
    }

    @Override
    public String toString(Object obj) {
        return Utils.urlEscape(((JID) obj).toString());
    }

}
