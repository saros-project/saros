import {
  ErrorHandler,
  Message,
  ErrorAction,
  CloseAction,
} from 'vscode-languageclient';

export type ErrorCallback = (reason?: any) => void;
const ErrorThreshold = 5;

/**
 * Error handler for the language client.
 *
 * @export
 * @class SarosErrorHandler
 * @implements {ErrorHandler}
 */
export class SarosErrorHandler implements ErrorHandler {
  private _lastClosedCount = 0;

  /**
   * Creates an instance of SarosErrorHandler.
   *
   * @param {ErrorCallback} _callback
   * @memberof SarosErrorHandler
   */
  constructor(private _callback: ErrorCallback) {}

  /**
   * Callback on errors.
   *
   * @param {Error} error The occured error
   * @param {Message} _message The error message
   * @param {number} count The error number
   * @return {ErrorAction} The resulting action
   * @memberof SarosErrorHandler
   */
  error(error: Error, _message: Message, count: number): ErrorAction {
    if (count <= ErrorThreshold) {
      return ErrorAction.Continue;
    }

    this._callback(error.message);
    return ErrorAction.Shutdown;
  }

  /**
   * Callback if connection has been closed.
   *
   * @return {CloseAction} The resulting action
   * @memberof SarosErrorHandler
   */
  closed(): CloseAction {
    this._lastClosedCount++;
    if (this._lastClosedCount <= ErrorThreshold) {
      return CloseAction.Restart;
    }

    return CloseAction.DoNotRestart;
  }
}
