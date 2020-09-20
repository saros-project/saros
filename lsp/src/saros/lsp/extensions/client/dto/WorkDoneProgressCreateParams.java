package saros.lsp.extensions.client.dto;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * WorkDoneProgressCreateParams implementation of the language server protocol.
 *
 * @see
 *     https://microsoft.github.io/language-server-protocol/specification#window_workDoneProgress_create
 */
public class WorkDoneProgressCreateParams {
  /** The progress token provided by the client or server. */
  public Either<String, Integer> token;

  public WorkDoneProgressCreateParams(String token) {
    this.token = Either.forLeft(token);
  }

  public WorkDoneProgressCreateParams(int token) {
    this.token = Either.forRight(token);
  }
}
