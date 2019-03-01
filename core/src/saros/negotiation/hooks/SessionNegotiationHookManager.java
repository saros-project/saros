package saros.negotiation.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This component manages extensions of the session negotiation by offering other components to hook
 * in (see {@link ISessionNegotiationHook}).
 */
public class SessionNegotiationHookManager {
  private List<ISessionNegotiationHook> hooks;

  public SessionNegotiationHookManager() {
    hooks = new CopyOnWriteArrayList<ISessionNegotiationHook>();
  }

  public void addHook(ISessionNegotiationHook hook) {
    hooks.add(hook);
  }

  public void removeHook(ISessionNegotiationHook hook) {
    hooks.remove(hook);
  }

  public List<ISessionNegotiationHook> getHooks() {
    return new ArrayList<ISessionNegotiationHook>(hooks);
  }
}
