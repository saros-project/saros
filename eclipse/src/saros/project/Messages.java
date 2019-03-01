package de.fu_berlin.inf.dpp.project;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "de.fu_berlin.inf.dpp.project.messages"; // $NON-NLS-1$
  public static String ResourceChangeValidator_error_leave_session_before_delete_project;
  public static String ResourceChangeValidator_error_no_write_access;

  public static String SarosSession_performing_permission_change;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {}
}
