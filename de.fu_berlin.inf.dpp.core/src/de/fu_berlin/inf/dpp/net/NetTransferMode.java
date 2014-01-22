package de.fu_berlin.inf.dpp.net;

public enum NetTransferMode {
    NONE("UKNOWN"), IBB("IBB"), SOCKS5_MEDIATED("SOCKS5 (mediated)"), SOCKS5_DIRECT(
        "SOCKS5 (direct)");

    private final String name;

    NetTransferMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}