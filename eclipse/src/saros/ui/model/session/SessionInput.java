package saros.ui.model.session;

import saros.session.ISarosSession;
import saros.ui.widgets.viewer.session.XMPPSessionDisplayComposite;

/**
 * Instances of this class bundle a custom content and an {@link ISarosSession} instance for use
 * with {@link XMPPSessionDisplayComposite}.
 */
public class SessionInput {

  private final Object customContent;
  private final ISarosSession session;

  public SessionInput(ISarosSession session, Object additionalContent) {
    this.session = session;
    this.customContent = additionalContent;
  }

  public Object getCustomContent() {
    return customContent;
  }

  public ISarosSession getSession() {
    return session;
  }
}
