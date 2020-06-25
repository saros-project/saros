package saros.session.resources.validation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

  private static final String BUNDLE_NAME =
      "saros.session.resources.validation.messages"; //$NON-NLS-1$

  public static String ResourceChangeValidator_ModifyingResourcesErrorMessage;

  public static String ResourceChangeValidator_DeleteReferencePointErrorMessage;

  public static String ResourceChangeValidator_MoveOrRenameReferencePointErrorMessage;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {}
}
