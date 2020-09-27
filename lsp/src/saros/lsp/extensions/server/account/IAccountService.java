package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.account.dto.AccountDto;
import saros.lsp.extensions.server.account.dto.AddInput;
import saros.lsp.extensions.server.account.dto.RemoveInput;
import saros.lsp.extensions.server.account.dto.SetActiveInput;
import saros.lsp.extensions.server.account.dto.UpdateInput;

/** Interface of the account service that is responsible for everything account related. */
@JsonSegment("saros/account")
public interface IAccountService {

  /**
   * Adds an existing account to the account store.
   *
   * @param input The account to add
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> add(AddInput input);

  /**
   * Updates an existing account in the account store.
   *
   * @param input The account to update
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> update(UpdateInput input);

  /**
   * Removes an existing account from the account store.
   *
   * @param input The account to remove
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> remove(RemoveInput input);

  /**
   * Sets an account as active, ie. it's used when connecting to the XMPP server.
   *
   * @param input The account to set active
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> setActive(SetActiveInput input);

  /**
   * Gets all accounts from the account store.
   *
   * @return A future with a result containing all accounts from the account store
   */
  @JsonRequest
  CompletableFuture<SarosResultResponse<AccountDto[]>> getAll();
}
