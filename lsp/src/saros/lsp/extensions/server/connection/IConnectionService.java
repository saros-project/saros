package saros.lsp.extensions.server.connection;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;

/** Interface of the contact service that is responsible for everything contact related. */
@JsonSegment("saros/connection")
public interface IConnectionService {

  /**
   * Connects to the XMPP server with the account that has been set active.
   *
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> connect();

  /**
   * Disconnects from the XMPP server.
   *
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> disconnect();

  /**
   * Gets the current connection state to the XMPP server.
   *
   * @return <i>true</i> if connection is active, <i>false</i> otherwise
   */
  @JsonRequest
  CompletableFuture<SarosResultResponse<Boolean>> state();
}
