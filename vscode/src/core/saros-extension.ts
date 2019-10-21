import { ExtensionContext, workspace, window } from "vscode";
import { SarosServer } from "./saros-server";
import { SarosClient } from "./saros-client";
import { LanguageClientOptions, RevealOutputChannelOn } from "vscode-languageclient";

/**
 * The Saros extension.
 */
export class SarosExtension {
    private context!: ExtensionContext;
    public client!: SarosClient;

    /**
     * Instanciates the Saros extension.
     */
    constructor() {
        
    }

    /**
     * Sets the context the extension runs on.
     * 
     * @param context - The extension context
     * @returns Itself
     */
    setContext(context: ExtensionContext): SarosExtension {
        this.context = context;

        return this;
    }

    /**
     * Initializes the extension.
     */
    init(): Promise<void> {
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
     */
    onReady(): Promise<void> {
        if(!this.client) {
            return Promise.reject('SarosExtension is not initialized');
        }

        return this.client.onReady();
    }

    /**
     * Creates the client options for the {@link vscode-languageclient#LanguageClient | language client}.
     * 
     * @returns The language client options
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