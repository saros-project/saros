import {LanguageClient} from 'vscode-languageclient';

/**
 * Response for adding new accounts.
 *
 * @export
 * @interface AddAccountResponse
 */
export interface AddAccountResponse {
    response: boolean;
}

/**
 * Request for adding new accounts.
 *
 * @export
 * @interface AddAccountRequest
 */
export interface AddAccountRequest {

}

/**
 * Custom language client for Saros protocol.
 *
 * @export
 * @class SarosClient
 * @extends {LanguageClient}
 */
export class SarosLangClient extends LanguageClient {
  /**
     * Adds a new account.
     *
     * @param {string} name - Account identifier
     * @return {Thenable<AddAccountResponse>} The result
     * @memberof SarosClient
     */
  addAccount(): Thenable<AddAccountResponse> {
    const request: AddAccountRequest = {

    };

    return this.sendRequest('saros/account/add', request);
  }
}
