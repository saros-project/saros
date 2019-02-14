package de.fu_berlin.inf.dpp.core.monitoring;

/**
 * This is a dummy implementation to ease the copy-paste-adapt process of creating Saros/I out of
 * Saros/E.
 *
 * <p>TODO Check whether this actually necessary
 */
public class Status implements IStatus {
  public static final Status CANCEL_STATUS = new Status(0);
  public static final Status OK_STATUS = new Status(1);

  public Status() {
    // Do nothing
  }

  public Status(int status) {
    // Do nothing
  }

  public Status(int status, String msg, String title) {
    // Do nothing
  }

  public Status(int status, String msg, String title, Exception ex) {
    // Do nothing
  }
}
