import {commands} from 'vscode';

export namespace variables {
    /**
     * Sets the initialization state of the extension.
     *
     * @export
     * @param {boolean} isInitialized The initialization state
     */
    export const setInitialized = (isInitialized: boolean) => {
      commands.executeCommand('setContext', 'initialized', isInitialized);
    };

    /**
     * Sets the connection state of the extension.
     *
     * @export
     * @param {boolean} isActive The connection state
     */
    export const setConnectionActive = (isActive: boolean) => {
      commands.executeCommand('setContext', 'connectionActive', isActive);
    };
}
