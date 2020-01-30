package saros.lsp.extensions.client;

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Interface of the Saros language client.
 *
 * <p>The language client is being used to interact with the connected client.
 *
 * <p>All client features that aren't covered by the lsp protocol have to be specified here.
 */
public interface ISarosLanguageClient extends LanguageClient {}
