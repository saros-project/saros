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
package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.DropSilentlyPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension.Content;
import de.fu_berlin.inf.dpp.util.xstream.XppReader;

public class ActivitiesExtensionProvider implements PacketExtensionProvider {

    private static final Logger log = Logger
        .getLogger(ActivitiesExtensionProvider.class.getName());

    public PacketExtension parseExtension(XmlPullParser parser) {
        Content content;
        try {
            content = (Content) ActivitiesPacketExtension.getXStream()
                .unmarshal(new XppReader(parser));
        } catch (RuntimeException e) {
            log.error("Malformed data received!", e);
            return new DropSilentlyPacketExtension();
        }
        return new ActivitiesPacketExtension(content.getSessionID(), content
            .getTimedActivities());
    }
}
