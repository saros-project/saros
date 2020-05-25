package saros.ui;

import saros.util.MessageUtils;

/** Core message bundle. */
public class CoreMessages {

  static {
    MessageUtils.initializeMessages(CoreMessages.class.getName(), CoreMessages.class);
  }

  private CoreMessages() {
    // NOP
  }

  public static String ConnectingFailureHandler_invalid_username_password_message;
  public static String ConnectingFailureHandler_only_sasl_allowed;
  public static String ConnectingFailureHandler_server_not_found;
  public static String ConnectingFailureHandler_server_not_connect;
  public static String ConnectingFailureHandler_title;
  public static String ConnectingFailureHandler_unknown_error_message;
}
