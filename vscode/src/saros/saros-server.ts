import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import { Saros } from '../core/types';
import { Socket } from 'net';

export class SarosServer implements Saros{
    constructor(private port: number, private ctx: vscode.ExtensionContext, private output: vscode.OutputChannel) {

    }

    public start() {
        this.startServer();
        this.startClient();
    }

    private startServer() {
        
        var extDir = path.join(this.ctx.extensionPath,'out', 'saros.jar');
        var proc = process.spawn(`java`, [`-jar`, `${extDir}`, `srv`, this.port.toString()]);
                
        proc.stdout.on("data", (data) => {
            this.output.appendLine(`[SERVER.STDOUT] ${data.toString()}`);
        });

        proc.stderr.on("data", (data) => {
            this.output.appendLine(`[SERVER.STDERR] ${data.toString()}`);
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
    }

    private startClient() {
        var socket = new Socket();
        let self = this;

        socket.connect(this.port, 'localhost', function() {
            self.output.appendLine('[CLIENT] connected');
        });

        socket.on('data', data => {
            self.output.appendLine(`[CLIENT.RCV] ${data.toString()}`);
        });

        socket.write('hello world from client!');
    }
}