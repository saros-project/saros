import { LanguageClient } from "vscode-languageclient";

/**
 * Response for adding new accounts.
 *
 * @export
 * @interface AddAccountResponse
 */
export interface AddAccountResponse {
    Response: boolean;
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
export class SarosClient extends LanguageClient {
    
    /**
     * Adds a new account.
     *
     * @param {string} name - Account identifier
     * @returns {Thenable<AddAccountResponse>} The result
     * @memberof SarosClient
     */
    addAccount(name: string): Thenable<AddAccountResponse> {
        const request: AddAccountRequest = {

        };

        return this.sendRequest("saros/account/add", request);
    }
}