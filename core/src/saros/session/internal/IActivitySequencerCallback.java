package saros.session.internal;

import saros.net.xmpp.JID;
import saros.session.User;

/**
 * Simple callback interface for monitoring events that occur in the {@link ActivitySequencer}
 * during session runtime. Implementing interfaces <b>must</b> ensure that they will <b>not</b>
 * block on any callback that is made.
 *
 * @author srossbach
 */
public interface IActivitySequencerCallback {

  /**
   * Gets called when a transmission failure occurs during activity sending. The user is already
   * unregistered at this point so there is not need to call {@link
   * ActivitySequencer#unregisterUser(User)}.
   *
   * @param jid the {@link JID} of the user that was unregistered from the {@linkplain
   *     ActivitySequencer sequencer}
   */
  public void transmissionFailed(JID jid);
}
