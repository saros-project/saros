package saros.lsp.activity;

import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;

/**
 * The InconsistencyHandler is responsible for asking the user if inconsistencies should be resolved
 * once they have been reported by the watchdog client.
 *
 * <p>If the user responds with a positive response the inconsistencies will be resolved by the
 * watchdog client.
 */
public class InconsistencyHandler extends AbstractActivityConsumer implements Startable {

  @Override
  public void start() {}

  @Override
  public void stop() {}
}
