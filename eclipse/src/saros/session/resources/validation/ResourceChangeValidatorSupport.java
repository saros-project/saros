package saros.session.resources.validation;

import saros.repackaged.picocontainer.Startable;
import saros.session.ISarosSession;

public class ResourceChangeValidatorSupport implements Startable {

  private final ISarosSession session;

  public ResourceChangeValidatorSupport(final ISarosSession session) {
    this.session = session;
  }

  @Override
  public void start() {
    ResourceChangeValidator.setSession(session);
  }

  @Override
  public void stop() {
    ResourceChangeValidator.setSession(null);
  }
}
