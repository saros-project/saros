package de.fu_berlin.inf.dpp.misc.xstream;

import java.nio.charset.Charset;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.log4j.Logger;

/**
 * Simple wrapper around {@link org.apache.commons.codec.net.URLCodec} using UTF-8 as default
 * charset.
 */
class URLCodec {

  private static final Logger LOG = Logger.getLogger(URLCodec.class);

  private static final Charset CHARSET = Charset.forName("UTF-8");

  private static final org.apache.commons.codec.net.URLCodec URL_CODEC =
      new org.apache.commons.codec.net.URLCodec(CHARSET.name());

  /** @see org.apache.commons.codec.net.URLCodec#encode(String) */
  static String encode(String data) {
    try {
      return URL_CODEC.encode(data);
    } catch (EncoderException e) {
      LOG.error("failed to encode data: " + data, e);
      return data;
    }
  }

  /** @see org.apache.commons.codec.net.URLCodec#decode(String) */
  static String decode(String data) {
    try {
      return URL_CODEC.decode(data);
    } catch (DecoderException e) {
      LOG.error("failed to decode data: " + data, e);
      return data;
    }
  }
}
