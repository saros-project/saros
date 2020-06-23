import {commands} from 'vscode';
import {SarosExtension} from '../core';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateAccounts(extension: SarosExtension) {
  commands.registerCommand('saros.account.add', () => {
    extension.onReady()
        .then(() => {
          return extension.client.addAccount();
        })
        .then((r) => {
          console.log('Response was: ' + r.response);
        });
  });
}
