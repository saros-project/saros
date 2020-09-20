package saros.lsp.extensions.server.account;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.account.dto.AccountDto;
import saros.lsp.extensions.server.account.dto.AddInput;
import saros.lsp.extensions.server.account.dto.RemoveInput;
import saros.lsp.extensions.server.account.dto.SetActiveInput;
import saros.lsp.extensions.server.account.dto.UpdateInput;
import saros.net.xmpp.JID;

/** Implementation of the account service. */
public class AccountService implements IAccountService {

  private final XMPPAccountStore accountStore;

  public AccountService(final XMPPAccountStore accountStore) {
    this.accountStore = accountStore;
  }

  @Override()
  public CompletableFuture<SarosResultResponse<AccountDto[]>> getAll() {

    final List<XMPPAccount> accounts = this.accountStore.getAllAccounts();
    final XMPPAccount defaultAccount = this.accountStore.getDefaultAccount();

    final AccountDto[] dtos =
        accounts
            .stream()
            .map(
                account -> {
                  AccountDto dto = new AccountDto();
                  dto.domain = account.getDomain();
                  dto.username = account.getUsername();
                  dto.password = account.getPassword();
                  dto.server = account.getServer();
                  dto.port = account.getPort();
                  dto.useTLS = account.useTLS();
                  dto.useSASL = account.useSASL();
                  dto.isDefault = account.equals(defaultAccount);

                  return dto;
                })
            .toArray(size -> new AccountDto[size]);

    return CompletableFuture.completedFuture(new SarosResultResponse<AccountDto[]>(dtos));
  }

  @Override
  public CompletableFuture<SarosResponse> update(final UpdateInput input) {

    try {
      JID jid = new JID(input.username);
      final XMPPAccount account = this.accountStore.getAccount(jid.getName(), jid.getDomain());
      this.accountStore.changeAccountData(
          account,
          input.username,
          input.password,
          input.domain,
          input.server,
          input.port,
          input.useTLS,
          input.useSASL);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> remove(final RemoveInput input) {

    try {
      final XMPPAccount account = this.accountStore.getAccount(input.username, input.domain);
      this.accountStore.deleteAccount(account);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> setActive(final SetActiveInput input) {

    try {
      final XMPPAccount account = this.accountStore.getAccount(input.username, input.domain);
      this.accountStore.setDefaultAccount(account);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> add(AddInput input) {
    try {
      this.accountStore.createAccount(
          input.username,
          input.password,
          input.domain,
          input.server,
          input.port,
          input.useTLS,
          input.useSASL);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }
}
