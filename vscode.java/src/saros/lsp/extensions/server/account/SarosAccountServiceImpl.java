package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;

public class SarosAccountServiceImpl implements SarosAccountService {

    @Override
    public CompletableFuture<SarosAddAccountResponse> add(SarosAddAccountRequest request) {
        SarosAddAccountResponse response = new SarosAddAccountResponse();
        response.Response = true;
        
        return CompletableFuture.completedFuture(response);
    }

}