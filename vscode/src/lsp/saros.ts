import {
  ExtensionContext,
  workspace,
  window,
  ProgressLocation,
  OutputChannel,
} from 'vscode';
import {SarosServer} from './sarosServer';
import {
  LanguageClientOptions,
  RevealOutputChannelOn,
} from 'vscode-languageclient';
import {config} from './sarosConfig';
import {SarosClient} from './sarosClient';
import * as _ from 'lodash';
import {IEventAggregator, EventAggregator} from '../types/eventAggregator';
import {SarosErrorHandler, ErrorCallback} from './sarosErrorHandler';

type SubscriptionCallback<TArgs> = (args: TArgs) => void;

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class Saros implements IEventAggregator {
    public context!: ExtensionContext;
    public client!: SarosClient;
    private server!: SarosServer;
    public channel: OutputChannel;

    private _eventAggregator = new EventAggregator();

    /**
     * Subscribes to an event.
     *
     * @template TArgs Event argument type
     * @param {string} event Event identifier
     * @param {SubscriptionCallback<TArgs>} callback Event callback
     * @memberof SarosExtension
     */
    public subscribe<TArgs>(event: string,
        callback: SubscriptionCallback<TArgs>) {
      this._eventAggregator.subscribe(event, callback);
    }

    /**
     * Publishes an event.
     *
     * @template TArgs Event argument type
     * @param {string} event Event identifier
     * @param {TArgs} args Event arguments
     * @memberof SarosExtension
     */
    public publish<TArgs>(event: string, args: TArgs) {
      this._eventAggregator.publish(event, args);
    }

    /**
     * Creates an instance of SarosExtension.
     *
     * @memberof SarosExtension
     */
    constructor() {
      this.channel = window.createOutputChannel('Saros');
    }

    /**
     * Sets the context the extension runs on.
     *
     * @param {ExtensionContext} context - The extension context
     * @return {Saros} Itself
     * @memberof SarosExtension
     */
    setContext(context: ExtensionContext): Saros {
      this.context = context;

      return this;
    }

    /**
     * Initializes the extension.
     *
     * @return {Promise<void>} Awaitable promise that
     *  returns after initialization
     * @memberof SarosExtension
     */
    async init(): Promise<void> {
      if (!this.context) {
        return Promise.reject(new Error('Context not set'));
      }

      const self = this;

      return window.withProgress({
        location: ProgressLocation.Window,
        cancellable: false,
        title: 'Starting Saros',
      },
      () => {
        return new Promise((resolve, reject) => {
          self.server = new SarosServer(self.context);
          self.client = new SarosClient(self.server.getStartFunc(reject),
              this._createClientOptions(reject));
          self.context.subscriptions.push(self.client.start());

          self.client.onReady().then(() => resolve());
        });
      });
    }

    /**
     * Callback when extension is ready.
     *
     * @return {Promise<void>} Awaitable promise that
     *  returns once extension is ready
     * @memberof SarosExtension
     */
    async onReady(): Promise<void> {
      if (!this.client) {
        return Promise.reject(new Error('SarosExtension is not initialized'));
      }

      return this.client.onReady();
    }

    /**
     * Creates the language client options.
     *
     * @private
     * @param {ErrorCallback} errorCallback Callback when client throws errors
     * @return {LanguageClientOptions} Used language client options
     * @memberof SarosExtension
     */
    private _createClientOptions(errorCallback: ErrorCallback)
      : LanguageClientOptions {
      const clientOptions: LanguageClientOptions = {
        documentSelector: [{scheme: 'file'}],
        synchronize: {
          configurationSection: config.appName,
          fileEvents: workspace.createFileSystemWatcher('**/*'),
        },
        revealOutputChannelOn: RevealOutputChannelOn.Error,
        errorHandler: new SarosErrorHandler(errorCallback),
        outputChannel: this.channel,
      };

      return clientOptions;
    }

    /**
     * Deactivates Saros.
     *
     * @memberof SarosExtension
     */
    public deactivate(): void {
      this.client?.stop();
      this.client?.outputChannel.dispose();
      this.server?.dispose();
    }
}

export const sarosInstance = new Saros();
