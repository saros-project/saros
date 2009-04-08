package de.fu_berlin.inf.dpp.util.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * Converter for URL encoding strings to use them as attribute values.
 */
public class UrlEncodingStringConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(String.class);
    }

    @Override
    public Object fromString(String s) {
        return Util.urlUnescape(s);
    }

    @Override
    public String toString(Object obj) {
        return Util.urlEscape((String) obj);
    }
}
