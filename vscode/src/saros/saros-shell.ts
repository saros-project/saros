import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import { Saros } from '../core/types';

export class SarosShell implements Saros {
    constructor(private ctx: vscode.ExtensionContext, private output: vscode.OutputChannel) {

    }

    public start() {
        
        var extDir = path.join(this.ctx.extensionPath,'out', 'saros.jar');
        var proc = process.spawn(`java`, [`-jar`, extDir, `sh`]/*, {stdio: "pipe"}*/);
                
        proc.stdout.on("data", (data) => {
            this.output.append(`[SHELL.STDOUT] ${data.toString()}`);
        });

        proc.stderr.on("data", (data) => {
            this.output.appendLine(`[SHELL.STDERR] ${data.toString()}`);
        });   

        proc.on('error', (error) => {
            vscode.window.showErrorMessage(`child process creating error with error ${error}`);
        });
    
        proc.on('close', (code) => {
            var showMessageFunc;
            if(code === 0) {
                showMessageFunc = vscode.window.showInformationMessage;
            } else {                
                showMessageFunc = vscode.window.showWarningMessage;
            }

            showMessageFunc(`child process exited with code ${code}`);
        });
        
        proc.stdin.write('hello world from vscode :)\n');
    }
}