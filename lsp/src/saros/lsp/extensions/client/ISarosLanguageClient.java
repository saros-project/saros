package saros.lsp.extensions.client;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import saros.lsp.extensions.client.dto.ProgressParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressCreateParams;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;

/**
 * Interface of the Saros language client.
 *
 * <p>The language client is being used to interact with the connected client.
 *
 * <p>All client features that aren't covered by the lsp protocol have to be specified here.
 */
public interface ISarosLanguageClient extends LanguageClient {

  /**
   * Sends a notification that informs the client about a state change of the XMPP connection, ie.
   * if it's active or not.
   *
   * @param isConnected <i>true</i> if XMPP connection is active, <i>false</i> otherwise
   */
  @JsonNotification("saros/connection/state")
  void sendStateConnected(SarosResultResponse<Boolean> isConnected);

  /**
   * Sends a notification that informs the client about a state change of a contact, eg. online
   * status or saros support.
   *
   * @param contact The contact whose state has changed
   */
  @JsonNotification("saros/contact/state")
  void sendStateContact(ContactDto contact);

  /**
   * Sends a request to the client to inform it about a new progress operation.
   *
   * @param params Details about the progress
   * @return A future without a value
   */
  @JsonRequest("window/workDoneProgress/create")
  CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params);

  /**
   * Sends a notification to the client to inform it about progress changes.
   *
   * @param <T>
   * @param params
   */
  @JsonNotification("$/progress")
  <T> void progress(ProgressParams<T> params);
}
