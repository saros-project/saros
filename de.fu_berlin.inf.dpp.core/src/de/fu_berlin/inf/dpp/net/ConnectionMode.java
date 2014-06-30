package de.fu_berlin.inf.dpp.net;

public enum ConnectionMode {
    NONE("UKNOWN"), IBB("IBB"), SOCKS5_MEDIATED("SOCKS5 (M)"), SOCKS5_DIRECT(
        "SOCKS5 (D)"), TCP("TCP");

    private final String name;

    ConnectionMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}