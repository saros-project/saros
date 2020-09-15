import {workspace} from 'vscode';

export namespace config {
    export enum ServerTrace {
        Off = 'off',
        Messages = 'messages',
        Verbose = 'verbose'
    }

    export enum ServerLog {
        All ='all',
        Debug = 'debug',
        Error = 'error',
        Fatal = 'fatal',
        Info = 'info',
        Off = 'off',
        Trace = 'trace',
        Warn = 'warn'
    }

    export const appName = 'saros';

    /**
     * Gets the configuration of the server trace level.
     *
     * @export
     * @return {ServerTrace} The server trace level
     */
    export const getTraceServer = () => {
      const trace = getConfiguration().get('trace.server') as ServerTrace;

      return trace;
    };

    /**
     * Gets the configuration of the server log level.
     *
     * @export
     * @return {ServerLog} The server log level
     */
    export const getLogServer = () => {
      const log = getConfiguration().get('log.server') as ServerLog;

      return log;
    };

    /**
     * Gets the configuration of the default host.
     *
     * @export
     * @return {string} The default host
     */
    export const getDefaultHost = () => {
      const host = getConfiguration().get('defaultHost.client') as string;

      return host;
    };

    /**
     * Gets the configuration of the server port.
     *
     * @export
     * @return {number} The server port
     */
    export const getServerPort = () => {
      const port = getConfiguration().get('port.server') as number;

      return port;
    };

    /**
     * Gets the configuration if the server is standalone.
     *
     * @export
     * @return {boolean} true if the server will be started
     *  externally and false otherwise
     */
    export const isServerStandalone = () => {
      const standalone = getConfiguration().get('standalone.server') as boolean;

      return standalone;
    };

    /**
     * Gets the configuration object.
     *
     * @return {WorkspaceConfiguration} The workspace configuration
     */
    const getConfiguration = () => {
      return workspace.getConfiguration(appName);
    };
}
