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

    VERSION_RESPONSE(0x2, VersionResponsePacket.class);

    private final short id;
    private final Class<?> clazz;

    private PacketType(int id, Class<?> clazz) {
        this.id = (short) (id & 0xFFFF);
        this.clazz = clazz;
    }

    public short getID() {
        return this.id;
    }

    public Class<?> getPacketClass() {
        return this.clazz;
    }

    public static final Map<Short, Class<?>> CLASS;

    static {
        Map<Short, Class<?>> clazz = new HashMap<Short, Class<?>>();

        for (PacketType type : PacketType.values())
            clazz.put(type.getID(), type.getPacketClass());

        CLASS = Collections.unmodifiableMap(clazz);
    }
}
