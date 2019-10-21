import { LanguageClient } from "vscode-languageclient";

export interface SarosAddAccountResponse {
    Response: boolean;
}

export interface SarosAddAccountRequest {

}

export class SarosClient extends LanguageClient {

    addAccount(name: string): Thenable<SarosAddAccountResponse> {
        const request: SarosAddAccountRequest = {

        };

        return this.sendRequest("saros/account/add", request);
    }
}