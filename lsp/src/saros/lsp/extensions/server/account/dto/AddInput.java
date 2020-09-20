package saros.lsp.extensions.server.account.dto;

/** Used to add an account to the account store. */
public class AddInput {
  public String username;

  public String password;

  public String domain;

  public String server;

  public int port;

  public Boolean useTLS;

  public Boolean useSASL;

  public Boolean isDefault;
}
