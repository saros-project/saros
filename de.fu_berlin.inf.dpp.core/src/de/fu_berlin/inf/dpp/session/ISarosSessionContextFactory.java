package de.fu_berlin.inf.dpp.session;

import org.picocontainer.MutablePicoContainer;

/** Responsible for creating the components of a {@link ISarosSession session}. */
public interface ISarosSessionContextFactory {

  /**
   * Creates all components required for a session and adds them to the given session context
   * container. The container's lifetime is equal to the lifetime of the session and is disposed
   * when the session ends.
   *
   * <p>When passed to this method, the session context container is already populated with the
   * {@link ISarosSession} it belongs to, which can thus be retrieved with <code>
   * container.getComponent(ISarosSession.class)</code>.
   *
   * @param session session to add components for
   * @param container DI container to add session components to
   */
  public void createComponents(ISarosSession session, MutablePicoContainer container);
}
