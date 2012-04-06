package de.fu_berlin.inf.dpp.net.packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PacketType {

    /*
     * WARNING: CHANGING THE ID WILL BREAK THE WHOLE SAROS PROTOCOL AND MAKE THE
     * NEXT SAROS VERSION INCOMPATIBLE TO ANY OLDER VERSIONS
     */

    NOP(0x0, NOPPacket.class),

    VERSION_REQUEST(0x1, VersionRequestPacket.class),

    VERSION_RESPONSE(0x2, VersionResponsePacket.class),

    TEST(0xFFFF, TestPacket.class);

    private final short id;
    private final Class<? extends Packet> clazz;

    private PacketType(int id, Class<? extends Packet> clazz) {
        this.id = (short) (id & 0xFFFF);
        this.clazz = clazz;
    }

    public short getID() {
        return this.id;
    }

    public Class<? extends Packet> getPacketClass() {
        return this.clazz;
    }

    public static final Map<Short, Class<? extends Packet>> CLASS;

    static {
        Map<Short, Class<? extends Packet>> clazz = new HashMap<Short, Class<? extends Packet>>();

        for (PacketType type : PacketType.values())
            clazz.put(type.getID(), type.getPacketClass());

        CLASS = Collections.unmodifiableMap(clazz);
    }
}
