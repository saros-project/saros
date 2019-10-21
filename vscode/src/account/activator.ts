import { SarosExtension } from "../core/saros-extension";
import { commands } from "vscode";

/**
 * Activation function of the account module.
 * 
 * @param extension - The instance of the extension
 */
export function activateAccounts(extension: SarosExtension) {
    commands.registerCommand('saros.account.add', () => {
        extension.onReady()
        .then(resolve => {
            return extension.client.addAccount('micha@jabber.com');
        })
        .then(r => {
            console.log('Response was: ' + r.Response);
        });
    });
}