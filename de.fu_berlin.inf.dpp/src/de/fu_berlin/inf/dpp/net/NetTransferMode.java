package de.fu_berlin.inf.dpp.net;

public enum NetTransferMode {
    NONE("UKNOWN", "", false), IBB("IBB", "XEP 47 In-Band Bytestream", false), SOCKS5(
        "SOCKS5", "XEP 65 SOCKS5", false), SOCKS5_MEDIATED("SOCKS5 (mediated)",
        "XEP 65 SOCKS5", false), SOCKS5_DIRECT("SOCKS5 (direct)",
        "XEP 65 SOCKS5", true);

    private String name;
    private String xep;
    private boolean direct;

    NetTransferMode(String name, String xep, boolean direct) {
        this.name = name;
        this.xep = xep;
        this.direct = direct;
    }

    public String getXEP() {
        return xep;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isP2P() {
        return direct;
    }
}