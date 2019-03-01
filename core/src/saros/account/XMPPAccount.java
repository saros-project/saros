package saros.account;

import java.io.Serializable;

/**
 * Representation of an XMPP account.
 *
 * @author Sebastian Schlaak
 * @author Stefan Rossbach
 */
public final class XMPPAccount implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;
  private String password;
  private String domain;
  private String server;

  private int port;

  /** Indicates if the account uses Transport Layer Security(TLS) protocol */
  private boolean useTLS;
  /** Indicates if the account uses Simple Authentication and Security Layer(SASL) protocol */
  private boolean useSASL;

  XMPPAccount(
      String username,
      String password,
      String domain,
      String server,
      int port,
      boolean useTLS,
      boolean useSASL) {

    if (username == null) throw new NullPointerException("user name is null");

    if (password == null) throw new NullPointerException("password is null");

    if (server == null) throw new NullPointerException("server is null");

    if (domain == null) throw new NullPointerException("domain is null");

    if (username.trim().length() == 0) throw new IllegalArgumentException("user name is empty");

    if (domain.trim().length() == 0) throw new IllegalArgumentException("domain is empty");

    if (!server.toLowerCase().equals(server))
      throw new IllegalArgumentException("server url must be in lower case letters");

    if (!domain.toLowerCase().equals(domain))
      throw new IllegalArgumentException("domain url must be in lower case letters");

    if (port < 0 || port >= 65536) throw new IllegalArgumentException("port number is not valid");

    if ((server.trim().length() != 0 && port == 0) || (server.trim().length() == 0 && port != 0))
      throw new IllegalArgumentException(
          "server or port value must not be unused if at least one value is used");

    this.username = username;
    this.password = password;
    this.server = server;
    this.domain = domain;
    this.port = port;

    this.useSASL = useSASL;
    this.useTLS = useTLS;
  }

  public boolean useSASL() {
    return this.useSASL;
  }

  void setUseSASL(boolean useSASL) {
    this.useSASL = useSASL;
  }

  public boolean useTLS() {
    return this.useTLS;
  }

  void setUseTLS(boolean useTLS) {
    this.useTLS = useTLS;
  }

  public int getPort() {
    return port;
  }

  void setPort(int port) {
    this.port = port;
  }

  public String getDomain() {
    return this.domain;
  }

  void setDomain(String domain) {
    this.domain = domain;
  }

  public String getUsername() {
    return username;
  }

  void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  void setPassword(String password) {
    this.password = password;
  }

  public String getServer() {
    return server;
  }

  void setServer(String server) {
    this.server = server;
  }

  @Override
  public String toString() {
    return "username: '"
        + username
        + "', domain: '"
        + domain
        + "', server: '"
        + server
        + "', port: "
        + port
        + ", TLS: "
        + useTLS
        + ", SASL: "
        + useSASL
        + " : ";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + server.hashCode();
    result = prime * result + username.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    XMPPAccount other = (XMPPAccount) obj;

    return this.username.equals(other.username)
        && this.server.equals(other.server)
        && this.domain.equals(other.domain)
        && this.port == other.port;
  }
}
