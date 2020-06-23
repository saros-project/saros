import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import * as net from 'net';
import {StreamInfo} from 'vscode-languageclient';

/**
 * Encapsulation of the Saros Language server.
 *
 * @export
 * @class SarosServer
 */
export class SarosLangServer {
    /**
     * Started process of the server.
     *
     * @private
     * @type {process.ChildProcess}
     * @memberof SarosServer
     */
    private process?: process.ChildProcess;

    /**
     * Creates an instance of SarosServer.
     *
     * @param {vscode.ExtensionContext} context - The extension context
     * @memberof SarosServer
     */
    constructor(private context: vscode.ExtensionContext) {

    }

    /**
     * Starts the server process.
     *
     * @param {number} port - The port the server listens on for connection
     * @memberof SarosServer
     */
    public start(port: number): void {
      if (this.process !== undefined) {
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
     * @return {function():Thenable<StreamInfo>} Function that starts the
     * server and retuns the stream information
     * @memberof SarosServer
     */
    public getStartFunc(): () => Thenable<StreamInfo> {
      return this.startServer;
    }

    /**
     * Starts the server on a random port.
     *
     * @return {Thenable<StreamInfo>} Stream information
     *  the server listens on
     * @memberof SarosServer
     */
    private startServer(): Thenable<StreamInfo> {
      const self = this;
      return new Promise((resolve) => {
        const server = net.createServer((socket) => {
          console.log('Creating server');

          resolve({
            reader: socket,
            writer: socket,
          });

          socket.on('end', () => console.log('Disconnected'));
        }).on('error', (err) => {
          // handle errors here
          throw err;
        });

        // grab a random port.
        server.listen(() => {
          const port = (server.address() as net.AddressInfo).port;

          self.start(port);
        });
      });
    }

    /**
     * Starts the Saros server jar as process.
     *
     * @private
     * @param {...any[]} args - Additional command line arguments for the server
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private startProcess(...args: any[]): SarosLangServer {
      const pathToJar = path.resolve(this.context.extensionPath,
          'dist', 'saros.lsp.jar');

      console.log('spawning jar process');
      this.process = process.spawn('java', ['-jar', pathToJar].concat(args));

      return this;
    }

    /**
     * Attaches listeners for debug informations and prints
     * retrieved data to a newly created
     * [output channel](#vscode.OutputChannel).
     *
     * @private
     * @param {boolean} isEnabled - Wether debug output is redirected or not
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private withDebug(isEnabled: boolean): SarosLangServer {
      if (this.process === undefined) {
        throw new Error('Server process is undefined');
      }

      if (!isEnabled) {
        return this;
      }

      const output = vscode.window.createOutputChannel('Saros (Debug)');

      this.process.stdout.on('data', (data) => {
        output.appendLine(data);
      });

      this.process.stderr.on('data', (data) => {
        output.appendLine(data);
      });

      return this;
    }

    /**
     * Attaches listeners to observe termination of the server.
     *
     * @private
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private withExitAware(): SarosLangServer {
      if (this.process === undefined) {
        throw new Error('Server process is undefined');
      }

      this.process.on('error', (error) => {
        vscode.window.showErrorMessage(
            `child process creating error with error ${error}`);
      });

      const self = this;
      this.process.on('close', (code) => {
        let showMessageFunc;
        if (code === 0) {
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
