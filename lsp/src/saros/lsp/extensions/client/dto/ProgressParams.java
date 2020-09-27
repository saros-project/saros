package saros.lsp.extensions.client.dto;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * ProgressParams implementation of the language server protocol.
 *
 * @see https://microsoft.github.io/language-server-protocol/specification#progress
 * @param <T> Progress payload
 */
public class ProgressParams<T> {

  /** The progress token provided by the client or server. */
  public Either<String, Integer> token;

  /** The progress data. */
  public T value;

  public ProgressParams(String token, T value) {
    this.token = Either.forLeft(token);
    this.value = value;
  }

  public ProgressParams(Integer token, T value) {
    this.token = Either.forRight(token);
    this.value = value;
  }
}
