package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import org.apache.log4j.Logger;

/**
 * Base class for synchronization processes
 *
 * @author jurke
 */
public abstract class SXESynchronization {

  public static final Logger log = Logger.getLogger(SXESynchronization.class);

  protected String peer;
  protected SXEController controller;

  protected SXESession session;

  public SXESynchronization(SXEController controller, SXESession session, String to) {
    assert (session != null);
    this.peer = to;
    this.session = session;
    this.controller = controller;
  }

  protected String prefix() {
    return "SXE(" + session.getSessionId() + ") ";
  }
}
