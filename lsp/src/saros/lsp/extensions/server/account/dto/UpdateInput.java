package saros.lsp.extensions.server.account.dto;

/** Used to update an account in the account store. */
public class UpdateInput {
  public String username;

  public String password;

  public String domain;

  public String server;

  public int port;

  public Boolean useTLS;

  public Boolean useSASL;

  public Boolean isDefault;
}
