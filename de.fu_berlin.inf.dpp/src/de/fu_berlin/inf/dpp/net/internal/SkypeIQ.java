package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.packet.IQ;

public class SkypeIQ extends IQ {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<query xmlns=\"jabber:iq:skype\">");

        if (this.name != null) {
            buf.append("<name>").append(this.name).append("</name>");
        }

        buf.append("</query>");
        return buf.toString();
    }
}
