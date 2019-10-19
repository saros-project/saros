import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import * as net from 'net';
import { StreamInfo } from 'vscode-languageclient';

export class SarosServer {
        
    constructor(private context: vscode.ExtensionContext) {

    }

    public start(port: number): void {
        this.startProcess(port);
    }

    public getStartFunc(): () => Thenable<StreamInfo> {
        
        let self = this;
        function createServer(): Thenable<StreamInfo> {
            return new Promise((resolve, reject) => {
                var server = net.createServer((socket) => {
                    console.log("Creating server");
    
                    resolve({
                        reader: socket,
                        writer: socket
                    });
    
                    socket.on('end', () => console.log("Disconnected"));
                }).on('error', (err) => {
                    // handle errors here
                    throw err;
                });			
    
                // grab a random port.
                server.listen(() => {
                    let port = (server.address() as net.AddressInfo).port;
                    
                    self.start(port);
                });
            });
        }

        return createServer;
    }
    
    private startProcess(...args: any[]): process.ChildProcess { //TODO: error handling
        
        var pathToJar = path.resolve(this.context.extensionPath, 'out', 'saros.vscode.java.jar');
        var jre = require('node-jre');

        console.log('spawning jar process');
        var proc = jre.spawn(
            [pathToJar],
            'saros.App',
            args,
            { encoding: 'utf8' }
        ) as process.ChildProcess;  
            
        
        this.addListeners(proc);

        return proc;
    }

    private addListeners(proc: process.ChildProcess): void {

        let output = vscode.window.createOutputChannel('Saros Server');

        proc.stdout.on("data", (data) => {
            output.appendLine(`[INFO] ${data.toString()}`);
        });

        proc.stderr.on("data", (data) => {
            output.appendLine(`[ERROR] ${data.toString()}`);
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
}