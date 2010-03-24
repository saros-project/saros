/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.PacketExtension;

public class DropSilentlyPacketExtension implements PacketExtension {

    // TODO This string constant is defined several times throughout the source.
    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String ELEMENT = "drop";

    public String getElementName() {
        return DropSilentlyPacketExtension.ELEMENT;
    }

    public String getNamespace() {
        return DropSilentlyPacketExtension.NAMESPACE;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(
            getNamespace()).append("\"/>");
        return buf.toString();
    }
}
