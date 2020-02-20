import {workspace, window, ExtensionContext} from 'vscode';
import {SarosLangServer} from './saros-lang-server';
import {SarosLangClient} from './saros-lang-client';
import {LanguageClientOptions,
  RevealOutputChannelOn} from 'vscode-languageclient';

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class SarosExtension {
    private context!: ExtensionContext;
    public client!: SarosLangClient;

    /**
     * Creates an instance of SarosExtension.
     *
     * @memberof SarosExtension
     */
    constructor() {

    }

    /**
     * Sets the context the extension runs on.
     *
     * @param {ExtensionContext} context - The extension context
     * @return {SarosExtension} Itself
     * @memberof SarosExtension
     */
    setContext(context: ExtensionContext): SarosExtension {
      this.context = context;

      return this;
    }

    /**
     * Initializes the extension.
     *
     * @memberof SarosExtension
     */
    async init() {
      if (!this.context) {
        return Promise.reject(new Error('Context not set'));
      }

      try {
        const self = this;

        return new Promise((resolve) => {
          const server = new SarosLangServer(self.context);
          self.client = new SarosLangClient('sarosServer', 'Saros Server',
              server.getStartFunc(), this.createClientOptions());
          this.context.subscriptions.push(self.client.start());

          resolve();
        });
      } catch (ex) {
        const msg = 'Error while activating plugin. ' +
            (ex.message ? ex.message : ex);
        return Promise.reject(new Error(msg));
      }
    }

    /**
     * Callback when extension is ready.
     *
     * @memberof SarosExtension
     */
    async onReady() {
      if (!this.client) {
        console.log('onReady.reject');
        return Promise.reject(new Error('SarosExtension is not initialized'));
      }

      console.log('onReady');
      return this.client.onReady();
    }

    /**
     * Creates the client options.
     *
     * @private
     * @return {LanguageClientOptions} The client options
     * @memberof SarosExtension
     */
    private createClientOptions(): LanguageClientOptions {
      const clientOptions: LanguageClientOptions = {
        documentSelector: ['plaintext'],
        synchronize: {
          fileEvents: workspace.createFileSystemWatcher('**/.clientrc'),
        },
        outputChannel: window.createOutputChannel('Saros'),
        revealOutputChannelOn: RevealOutputChannelOn.Info,
      };

      return clientOptions;
    }
}

export const sarosExtensionInstance = new SarosExtension();
