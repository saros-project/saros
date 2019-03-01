package de.fu_berlin.inf.dpp.intellij.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

public class MessageUtils {
  private static final Logger log = Logger.getLogger(MessageUtils.class);

  /**
   * Loads strings in to static variables from property file.
   *
   * @param propertyFile
   * @param clazz
   */
  public static void initializeMessages(String propertyFile, Class clazz) {
    try {
      log.info("Loading bundle [" + propertyFile + "]");

      ResourceBundle resourceBundle = ResourceBundle.getBundle(propertyFile, Locale.getDefault());

      for (Field f : clazz.getFields()) {
        String fieldName = f.getName();
        String fieldValue = resourceBundle.getString(fieldName);
        int modifier = f.getModifiers();
        if (Modifier.isPublic(modifier)
            && !Modifier.isFinal(modifier)
            && f.getType().equals(String.class)) {
          f.set(clazz, fieldValue);
        }
      }
    } catch (Exception e) {
      // it can not happen anyway!
    }
  }
}
