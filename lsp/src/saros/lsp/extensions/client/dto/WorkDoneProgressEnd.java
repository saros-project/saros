package saros.lsp.extensions.client.dto;

/**
 * WorkDoneProgressEnd implementation of the language server protocol.
 *
 * @see https://microsoft.github.io/language-server-protocol/specification#workDoneProgressEnd
 */
public class WorkDoneProgressEnd {
  String kind = "end";

  /** Optional, a final message indicating to for example indicate the outcome of the operation. */
  String message;

  public WorkDoneProgressEnd(String message) {
    this.message = message;
  }
}
