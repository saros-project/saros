import * as vscode from 'vscode';
import * as path from 'path';
import { Saros } from "../core/types";

export class SarosJavaJRE implements Saros {
    constructor(private ctx: vscode.ExtensionContext, private output: vscode.OutputChannel) {

    }

    start(): void {
        this.output.appendLine("starting");

        try {
            var extDir = path.join(this.ctx.extensionPath,'out', 'saros.vscode.java.jar');
            var jre = require('node-jre');

            var output = jre.spawnSync(  // call synchronously
                [extDir],                // add the relative directory 'java' to the class-path
                'saros.App',                 // call main routine in class 'Hello'
                ['core'],               // pass 'World' as only parameter
                { encoding: 'utf8' }     // encode output as string
            ).stdout.trim();           // take output from stdout as trimmed String

            this.output.appendLine("[JRE] " + output);
            
        } catch(exception) {
            this.output.appendLine(exception);
        }
    }
}