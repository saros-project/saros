package saros.net.stream;

public enum StreamMode {
  NONE("UNKNOWN"),
  IBB("IBB"),
  SOCKS5_MEDIATED("SOCKS5 (M)"),
  SOCKS5_DIRECT("SOCKS5 (D)"),
  TCP("TCP");

  private final String name;

  StreamMode(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
