package de.fu_berlin.inf.dpp.ui.widgetGallery.util;

import java.util.UUID;

public class StringUtils {
  public static String genRandom(int length) {
    StringBuffer sb = new StringBuffer();
    while (sb.length() < length) {
      String random = UUID.randomUUID().toString().replace("-", "");
      sb.append(random);
    }
    return sb.toString().substring(0, length);
  }
}
