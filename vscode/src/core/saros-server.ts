import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import * as net from 'net';
import { StreamInfo } from 'vscode-languageclient';

/**
 * Encapsulation of the Saros server.
 */
export class SarosServer {
    
    private process?: process.ChildProcess;

    /**
     * Instanciates the Saros server.
     * 
     * @param context - The extension context
     */
    constructor(private context: vscode.ExtensionContext) {
                
    }

    /**
     * Stats the server process.
     * 
     * @param port - The port the server listens on for connection 
     */
    public start(port: number): void {

        if(this.process !== undefined) {
            throw new Error('Server process is still running');
        }

        this.startProcess(port)
            .withDebug(true)
            .withExitAware();
    }

    /**
     * Provides access to the start function.
     * 
     * @remarks A free port will be determined and used.
     * @returns Function that starts the server and retuns the {@link vscode-languageclient#StreamInfo | io access}
     */
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
    
    /**
     * Starts the Saros server jar as process.
     * 
     * @param args - Additional command line arguments for the server
     * @returns Itself 
     */
    private startProcess(...args: any[]): SarosServer {
        
        var pathToJar = path.resolve(this.context.extensionPath, 'out', 'saros.vscode.java.jar');
        var jre = require('node-jre');

        console.log('spawning jar process');
        this.process = jre.spawn(
            [pathToJar],
            'saros.lsp.SarosLauncher',
            args,
            { encoding: 'utf8' }
        ) as process.ChildProcess;  

        return this;
    }

    /**
     * Attaches listeners for debug informations and prints
     * retrieved data to a newly created {@link vscode#OutputChannel | output channel}.
     * 
     * @param isEnabled - Wether debug output is redirected or not
     * @returns Itself
     */
    private withDebug(isEnabled: boolean): SarosServer {

        if(this.process === undefined) {
            throw new Error('Server process is undefined');
        } 

        if(!isEnabled) {
            return this;
        }

        let output = vscode.window.createOutputChannel('Saros (Debug)');

        this.process.stdout.on("data", (data) => {
            output.appendLine(data);
        });

        this.process.stderr.on("data", (data) => {
            output.appendLine(data);
        });   

        return this;
    }

    /**
     * Attaches listeners to observe terminations of the server.
     * 
     * @returns Itself
     */
    private withExitAware(): SarosServer {  

        if(this.process === undefined) {
            throw new Error('Server process is undefined');
        }

        this.process.on('error', (error) => {
            vscode.window.showErrorMessage(`child process creating error with error ${error}`);
        });
    
        let self = this;
        this.process.on('close', (code) => {
            var showMessageFunc;
            if(code === 0) {
                showMessageFunc = vscode.window.showInformationMessage;
            } else {                
                showMessageFunc = vscode.window.showWarningMessage;
            }

            self.process = undefined;
            showMessageFunc(`child process exited with code ${code}`);            
        });

        return this;
    }
}