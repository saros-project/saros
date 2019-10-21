import { LanguageClient } from "vscode-languageclient";

/**
 * Interface of the addAccount response.
 */
export interface AddAccountResponse {
    Response: boolean;
}

/**
 * Interface of the addAccount request.
 */
export interface AddAccountRequest {

}

/**
 * Extension of the {@link vscode-languageclient#LanguageClient | Language Client} 
 * that supports the Saros protocol.
 */
export class SarosClient extends LanguageClient {

    /**
     * Adds a new account.
     * 
     * @param name - Account identifier
     * @returns The response of Saros
     * 
     * @experimental
     */
    addAccount(name: string): Thenable<AddAccountResponse> {
        const request: AddAccountRequest = {

        };

        return this.sendRequest("saros/account/add", request);
    }
}