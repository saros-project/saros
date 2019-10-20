package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("saros/account")
public interface SarosAccountService {
    @JsonRequest
    CompletableFuture<SarosAddAccountResponse> add(SarosAddAccountRequest request);
}