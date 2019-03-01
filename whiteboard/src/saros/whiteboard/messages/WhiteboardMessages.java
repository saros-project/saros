package saros.whiteboard.messages;

import org.eclipse.osgi.util.NLS;

public class WhiteboardMessages extends NLS {
  private static final String BUNDLE_NAME = "saros.whiteboard.messages"; // $NON-NLS-1$
  public static String export_description;
  public static String export_text;
  public static String export_dialog_text;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, WhiteboardMessages.class);
  }

  private WhiteboardMessages() {}
}
