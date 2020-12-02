package saros.lsp.net.session;

import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.session.INegotiationHandler;

/** Implementation of {@link INegotiationHandler}. */
public class NegotiationHandler implements INegotiationHandler {

  @Override
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {}

  @Override
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {}

  @Override
  public void handleOutgoingResourceNegotiation(AbstractOutgoingResourceNegotiation negotiation) {}

  @Override
  public void handleIncomingResourceNegotiation(AbstractIncomingResourceNegotiation negotiation) {}
}
