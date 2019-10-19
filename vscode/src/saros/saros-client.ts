import * as vscode from 'vscode';
import { Disposable } from "vscode-jsonrpc";
import { StreamInfo, LanguageClientOptions, LanguageClient } from "vscode-languageclient";

export class SarosClient {

    public start(serverOptions: () => Thenable<StreamInfo>): Disposable {             

        return new LanguageClient('sarosServer', 'Saros Server', serverOptions, this.createClientOptions()).start();
    }

    private createClientOptions(): LanguageClientOptions {
        let clientOptions: LanguageClientOptions = {
            // Register the server for plain text documents
            documentSelector: ['plaintext'],
            synchronize: {
                // Synchronize the setting section 'languageServerExample' to the server
                configurationSection: 'languageServerExample',
                // Notify the server about file changes to '.clientrc files contain in the workspace
                fileEvents: vscode.workspace.createFileSystemWatcher('**/.clientrc')
            },
            outputChannel: vscode.window.createOutputChannel('Saros Client')
        };

        return clientOptions;
    }
}