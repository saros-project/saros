package saros.lsp.extensions.client;

/** Interface for Saros language client awareness. */
public interface ISarosLanguageClientAware {

  /**
   * Registers the language client.
   *
   * @param client connected language client
   */
  void connect(ISarosLanguageClient client);
}
