package saros.lsp.extensions.client;

/** Interface for Saros language client awareness. */
public interface SarosLanguageClientAware {

  /**
   * Registers the language client.
   *
   * @param client connected language client
   */
  void connect(SarosLanguageClient client);
}
