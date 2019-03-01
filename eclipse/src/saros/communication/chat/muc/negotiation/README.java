package saros.communication.chat.muc.negotiation;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * The MUCNegotiationManager must not be part of the communication package. This is a component used
 * during session negotiation to hold data that a client may need to start a MUC session. It was
 * hidden in that package, maybe gave some developers a headache on how MUC sessions are actually
 * started.
 */
public class README {

  /*
   * the only purpose this class serves it to create compile errors when moved
   * into the core ... delete the class when the issues are solved
   */

  static {
    System.err.println(ResourcesPlugin.getWorkspace() == null);
  }
}
