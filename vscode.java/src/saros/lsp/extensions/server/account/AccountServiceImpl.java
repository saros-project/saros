package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the account service.
 */
public class AccountServiceImpl implements AccountService {

    @Override
    public CompletableFuture<AddAccountResponse> add(AddAccountRequest request) {
        AddAccountResponse response = new AddAccountResponse();
        response.Response = true;
        
        return CompletableFuture.completedFuture(response);
    }

}