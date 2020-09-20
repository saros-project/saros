package saros.lsp.extensions.server.account.dto;

/** The dto represents an account which is used for connecting to a XMPP server. */
public class AccountDto {
  public String username;

  public String password;

  public String domain;

  public String server;

  public int port;

  public Boolean useTLS;

  public Boolean useSASL;

  public Boolean isDefault;
}
