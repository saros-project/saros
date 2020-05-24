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

  /** initial file */
}
