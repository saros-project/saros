package saros.lsp.extensions.server.contact;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.AddInput;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.contact.dto.RemoveInput;
import saros.lsp.extensions.server.contact.dto.RenameInput;

/** Interface of the contact service that is responsible for everything XMPP connection related. */
@JsonSegment("saros/contact")
public interface IContactService {

  /**
   * Adds a contact to the contact list.
   *
   * @param input The contact to add
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> add(AddInput input);

  /**
   * Removes a contact from the contact list.
   *
   * @param input The contact to remove
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> remove(RemoveInput input);

  /**
   * Renames a contact on the contact list.
   *
   * @param input The contact to rename
   * @return A future with a result indicating if the request has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> rename(RenameInput input);

  /**
   * Gets all contacts from the contact list.
   *
   * @return A future with a result containing all contacts from the contact list
   */
  @JsonRequest
  CompletableFuture<SarosResultResponse<ContactDto[]>> getAll();
}
