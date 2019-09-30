// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { SarosServer } from './saros/saros-server';
import { SarosShell } from './saros/saros-shell';
import { Saros } from './core/types';

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "saros" is now active!');

	var output = vscode.window.createOutputChannel('Saros');

	output.show(true);

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with registerCommand
	// The commandId parameter must match the command field in package.json
	let disposable = vscode.commands.registerCommand('extension.startSaros', async () => {
		// The code you place here will be executed every time your command is executed

		getSarosImpl(context, output).then(saros => {
			if(saros !== undefined) {
				saros.start();
			}
		});
	});

	context.subscriptions.push(disposable);
}

function getSarosImpl(context: vscode.ExtensionContext, output: vscode.OutputChannel): Thenable<Saros | undefined> {
	return vscode.window.showQuickPick(['local server', 'shell'], {placeHolder: 'Please pick saros type:'}).then<Saros | undefined>(pick => {
		switch(pick) {
			case 'local server':
				return new SarosServer(1234, context, output);
			case 'shell':
				return new SarosShell(context, output);
			default: 
				return undefined;
		}
	});
}

// this method is called when your extension is deactivated
export function deactivate() {
	console.log("deactivated");
}
