import {
  LanguageClient,
  LanguageClientOptions,
} from 'vscode-languageclient';
import {
  ConnectedStateNotification,
} from './sarosProtocol';
import {LanguageServerOptions} from './sarosServer';

type Callback<T> = (p: T) => void;

/**
 * Custom language client for the Saros protocol.
 *
 * @export
 * @class SarosClient
 * @extends {LanguageClient}
 */
export class SarosClient extends LanguageClient {
    private _connectionChangedListeners: Callback<boolean>[] = [];

    /**
     * Creates an instance of SarosClient.
     *
     * @param {LanguageServerOptions} serverOptions The server options
     * @param {LanguageClientOptions} clientOptions The client options
     * @memberof SarosClient
     */
    constructor(serverOptions: LanguageServerOptions,
        clientOptions: LanguageClientOptions) {
      super('saros', 'Saros Server', serverOptions, clientOptions, true);

      this.onReady().then(() => {
        this.onNotification(ConnectedStateNotification.type, (isOnline) => {
          this._connectionChangedListeners.forEach(
              (callback) => callback(isOnline.result),
          );
        });
      });
    }

    /**
     * Registers a callback for the connection changed event.
     *
     * @param {Callback<boolean>} callback The callback to execute
     * @memberof SarosClient
     */
    public onConnectionChanged(callback: Callback<boolean>) {
      this._connectionChangedListeners.push(callback);
    }
}
