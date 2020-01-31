import { ExtensionContext, workspace, window } from "vscode";
import { SarosServer } from "./saros-server";
import { SarosClient } from "./saros-client";
import { LanguageClientOptions, RevealOutputChannelOn } from "vscode-languageclient";

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class SarosExtension {
    private context!: ExtensionContext;
    public client!: SarosClient;

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
     * @returns {SarosExtension} Itself
     * @memberof SarosExtension
     */
    setContext(context: ExtensionContext): SarosExtension {
        this.context = context;

        return this;
    }

    /**
     * Initializes the extension.
     *
     * @returns
     * @memberof SarosExtension
     */
    async init() {
        
        if(!this.context) {
            return Promise.reject('Context not set');
        }

        try {                    
            let self = this;

            return new Promise((resolve, reject) => {
                
                const server = new SarosServer(self.context);
                self.client = new SarosClient('sarosServer', 'Saros Server', server.getStartFunc(), this.createClientOptions());          
                self.context.subscriptions.push(self.client.start());

                resolve();
            });
        } catch(ex) {
            const msg = "Error while activating plugin. " + (ex.message ? ex.message : ex);
            return Promise.reject(msg);
        }
    }

    /**
     * Callback when extension is ready.
     *
     * @returns
     * @memberof SarosExtension
     */
    async onReady() {
        if(!this.client) {
            console.log("onReady.reject");
            return Promise.reject('SarosExtension is not initialized');
        }

        console.log("onReady");
        return this.client.onReady();
    }

    /**
     * Creates the client options.
     *
     * @private
     * @returns {LanguageClientOptions} The client options
     * @memberof SarosExtension
     */
    private createClientOptions(): LanguageClientOptions {
        let clientOptions: LanguageClientOptions = {
            // Register the server for plain text documents
            documentSelector: ['plaintext'],
            synchronize: {
                // Synchronize the setting section 'languageServerExample' to the server
                //configurationSection: 'sarosServer',
                // Notify the server about file changes to '.clientrc files contain in the workspace
                fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
            },
            outputChannel: window.createOutputChannel('Saros'),
            revealOutputChannelOn: RevealOutputChannelOn.Info
        };

        return clientOptions;
    }
}

export const sarosExtensionInstance = new SarosExtension();