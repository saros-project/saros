package saros.whiteboard.sxe.net;

import org.apache.log4j.Logger;
import saros.whiteboard.sxe.SXEController;

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
