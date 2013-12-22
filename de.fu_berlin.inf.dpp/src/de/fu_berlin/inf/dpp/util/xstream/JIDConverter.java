package de.fu_berlin.inf.dpp.util.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.net.JID;

public class JIDConverter extends AbstractSingleValueConverter {

    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean canConvert(Class type) {
        return type.equals(JID.class);
    }

    @Override
    public Object fromString(String str) {
        return new JID(URLCodec.decode(str));
    }

    @Override
    public String toString(Object obj) {
        return URLCodec.encode(((JID) obj).toString());
    }

}
