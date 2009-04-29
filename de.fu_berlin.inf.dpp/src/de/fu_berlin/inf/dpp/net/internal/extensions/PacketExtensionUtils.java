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

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * Holds various simple helper methods to create and parse simple Smack packet
 * extensions.
 * 
 * @author rdjemili
 */
public class PacketExtensionUtils {

    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String SESSION_ID = "sessionID";

    public static final String DESCRIPTION = "description";

    public static final String PROJECTNAME = "description";

    public static final String COLOR_ID = "ColorID";

    public static void hookExtensionProviders() {

        ProviderManager providermanager = ProviderManager.getInstance();
        providermanager.addExtensionProvider(ActivitiesPacketExtension.ELEMENT,
            PacketExtensionUtils.NAMESPACE, new ActivitiesExtensionProvider());
    }

    public static ActivitiesPacketExtension getActvitiesExtension(
        Message message) {
        return (ActivitiesPacketExtension) message.getExtension(
            ActivitiesPacketExtension.ELEMENT, PacketExtensionUtils.NAMESPACE);
    }

    /**
     * @return true if message contains a JupiterRequest
     */
    public static boolean containsJupiterRequest(Message message) {
        ActivitiesPacketExtension extension = (ActivitiesPacketExtension) message
            .getExtension(ActivitiesPacketExtension.ELEMENT,
                PacketExtensionUtils.NAMESPACE);
        if (extension != null) {
            for (TimedActivity timedActivity : extension.getActivities()) {
                if (timedActivity.getActivity() instanceof Request)
                    return true;
            }
        }
        return false;
    }

    /**
     * TODO CJ: write javadoc
     * 
     * @param message
     * @return
     */
    public static String getSessionID(Message message) {
        PacketExtension extension = message.getExtension(
            ActivitiesPacketExtension.ELEMENT, PacketExtensionUtils.NAMESPACE);
        if (extension != null) {
            return ((ActivitiesPacketExtension) extension).getSessionID();
        }
        extension = message.getExtension(PacketExtensionUtils.NAMESPACE);
        return ((DefaultPacketExtension) extension).getValue(SESSION_ID);
    }

    /**
     * @return A PacketFilter that only accepts Packets if there is currently a
     *         SharedProject
     */
    public static PacketFilter getInSessionFilter(
        final SessionManager sessionManager) {
        return new PacketFilter() {
            public boolean accept(Packet arg0) {
                return sessionManager.getSharedProject() != null;
            }
        };
    }

    /**
     * @return filter that returns true iff currently a shared project exists
     *         and the message was from the host of this shared project.
     */
    public static PacketFilter getFromHostFilter(
        final SessionManager sessionManager) {
        return new PacketFilter() {
            public boolean accept(Packet packet) {
                ISharedProject project = sessionManager.getSharedProject();

                return project != null
                    && project.getHost().getJID().equals(
                        new JID(packet.getFrom()));
            }
        };
    }

    /**
     * @return PacketFilter that only accepts Messages (!) which belong to the
     *         current session
     */
    public static PacketFilter getSessionIDPacketFilter(
        final SessionIDObservable sessionIDObservable) {

        return new AndFilter(new MessageTypeFilter(Message.Type.chat),
            new PacketFilter() {
                public boolean accept(Packet arg0) {
                    Message message = (Message) arg0;

                    return sessionIDObservable.getValue().equals(
                        PacketExtensionUtils.getSessionID(message));
                }
            });
    }
}
