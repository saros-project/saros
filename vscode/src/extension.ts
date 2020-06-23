import * as vscode from 'vscode';
import {sarosExtensionInstance} from './core';
import {activateAccounts} from './account';

/**
 * Activation function of the extension.
 *
 * @export
 * @param {vscode.ExtensionContext} context - The extension context
 */
export function activate(context: vscode.ExtensionContext) {
  sarosExtensionInstance.setContext(context)
      .init()
      .then(() => {
        activateAccounts(sarosExtensionInstance);

        console.log('Extension "Saros" is now active!');
      })
      .catch((reason) => {
        console.log(reason);
        vscode.window.showErrorMessage(
            'Saros extension did not start propertly.' +
            'Reason: ' + reason);
      });

  context.subscriptions.push(createStatusBar());
}

/**
 * Creates the status bar.
 *
 * @return {Disposable} The status bar item as [disposable](#Disposable)
 */
function createStatusBar(): vscode.Disposable {
  const statusBarItem =
    vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left,
        Number.MAX_VALUE);
  statusBarItem.text = 'Saros';
  statusBarItem.show();

  return statusBarItem;
}

/**
 * Deactivation function of the extension.
 *
 * @export
 */
export function deactivate() {
  console.log('deactivated');
}
