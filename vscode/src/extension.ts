import * as vscode from 'vscode';
import { Disposable } from 'vscode-jsonrpc';
import { sarosExtensionInstance } from './core/saros-extension';
import { activateAccounts } from './account/activator';

export function activate(context: vscode.ExtensionContext) {

	sarosExtensionInstance.setContext(context)
						.init()
						.then(() => {
							console.log('Extension "Saros" is now active!');
						})
						.catch(reason => {
							vscode.window.showErrorMessage('Saros extension did not start propertly.'
														+ 'Reason: ' + reason); //TODO: restart feature
						});	
	
	context.subscriptions.push(createStatusBar());
}

function createStatusBar(): Disposable {

	let statusBarItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, Number.MAX_VALUE);
    statusBarItem.text = "Saros";
    statusBarItem.command = "saros.start";
	statusBarItem.show();
	
	return statusBarItem;
}

export function deactivate() {
	console.log("deactivated");
}
