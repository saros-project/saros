import * as vscode from 'vscode';
import { SarosServer } from './saros/saros-server';
import { SarosClient } from './saros/saros-client';
import { Disposable } from 'vscode-jsonrpc';

export function activate(context: vscode.ExtensionContext) {

	console.log('Extension "Saros" is now active!');
	
	context.subscriptions.push(createStatusBar());	

	let disposable = vscode.commands.registerCommand('saros.start', async () => {
				
		vscode.window.withProgress({location: vscode.ProgressLocation.Window, title: 'Saros: Starting'}, (progress, token) => {

			return new Promise(resolve => {
				let server = new SarosServer(context);
				let client = new SarosClient();
				
				context.subscriptions.push(client.start(server.getStartFunc()));

				resolve();
			});
		});
						
	});

	context.subscriptions.push(disposable);
}

function createStatusBar(): Disposable {

	let statusBarItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, Number.MAX_VALUE);
    statusBarItem.text = "Saros";
    statusBarItem.command = "saros.start";
	statusBarItem.show();
	
	return statusBarItem;
}

export function deactivate() {
	console.log("deactivated"); //TODO: remove status bar?
}
