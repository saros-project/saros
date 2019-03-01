package de.fu_berlin.inf.dpp.misc.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/** Converter for URL encoding strings to use them as attribute values. */
public class UrlEncodingStringConverter extends AbstractSingleValueConverter {

  @SuppressWarnings({"rawtypes"})
  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(String.class);
  }

  @Override
  public Object fromString(String s) {
    return URLCodec.decode(s);
  }

  @Override
  public String toString(Object obj) {
    return URLCodec.encode((String) obj);
  }
}
