package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/** Interface of the account service. */
@JsonSegment("saros/account")
public interface AccountService {

  /**
   * Adds a new account.
   *
   * @param request arguments of the request
   * @return response for the request
   */
  @JsonRequest
  CompletableFuture<AddAccountResponse> add(AddAccountRequest request);
}
