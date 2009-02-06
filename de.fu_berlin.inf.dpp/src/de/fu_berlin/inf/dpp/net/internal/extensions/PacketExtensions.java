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
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;

/**
 * Holds various simple helper methods to create and parse simple Smack packet
 * extensions.
 * 
 * @author rdjemili
 * 
 *         TODO This class should be converted to many small classes
 *         implementing the SarosPacketExtension and subclasses.
 */
public class PacketExtensions {

    public static abstract class SarosPacketExtension {

        public SarosPacketExtension(String element) {
            this.element = element;
        }

        String element;

        public PacketFilter getFilter() {
            return new PacketExtensionFilter(element, NAMESPACE);
        }

        public boolean hasExtension(Message m) {
            return m.getExtension(element, NAMESPACE) != null;
        }
    }

    public static abstract class SarosDefaultPackageExtension extends
        SarosPacketExtension {

        public SarosDefaultPackageExtension(String element) {
            super(element);
        }

        public DefaultPacketExtension create() {
            DefaultPacketExtension extension = new DefaultPacketExtension(
                element, NAMESPACE);
            return extension;
        }

        public DefaultPacketExtension getExtension(Message message) {
            return (DefaultPacketExtension) message.getExtension(element,
                NAMESPACE);
        }
    }

    /**
     * Abstract base class for all DefaultPacketExtension that need to include
     * the current SessionID
     */
    public static abstract class SessionDefaultPacketExtension extends
        SarosPacketExtension {

        public SessionDefaultPacketExtension(String element) {
            super(element);
        }

        public DefaultPacketExtension create() {
            DefaultPacketExtension extension = new DefaultPacketExtension(
                element, NAMESPACE);

            extension.setValue(PacketExtensions.SESSION_ID, getSessionID());

            return extension;
        }

        public DefaultPacketExtension getExtension(Message message) {
            return (DefaultPacketExtension) message.getExtension(element,
                NAMESPACE);
        }
    }

    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    // attributes
    public static final String SESSION_ID = "sessionID";

    public static final String DESCRIPTION = "description";

    public static final String PROJECTNAME = "description";

    public static final String ERROR = "error";

    public static final String DT_NAME = "DTNAME";

    public static final String DT_DESC = "DTDESC";

    public static final String DT_SPLIT = "DTSPLIT";

    public static final String DT_DATA = "DATA_BASE64";

    public static final String FILE_PATH = "filename";

    public static final String COLOR_ID = "ColorID";

    public static void hookExtensionProviders() {

        ProviderManager providermanager = ProviderManager.getInstance();
        providermanager.addExtensionProvider(ActivitiesPacketExtension.ELEMENT,
            PacketExtensions.NAMESPACE, new ActivitiesProvider());
        providermanager.addExtensionProvider(RequestPacketExtension.ELEMENT,
            RequestPacketExtension.NAMESPACE, new RequestExtensionProvider());
    }

    public static String getSessionID() {
        return Saros.getDefault().getSessionManager().getSessionID();
    }

    public static ActivitiesPacketExtension getActvitiesExtension(
        Message message) {
        return (ActivitiesPacketExtension) message.getExtension(
            ActivitiesPacketExtension.ELEMENT, PacketExtensions.NAMESPACE);
    }

    public static RequestPacketExtension getJupiterRequestExtension(
        Message message) {
        return (RequestPacketExtension) message.getExtension(
            RequestPacketExtension.ELEMENT, PacketExtensions.NAMESPACE);
    }

    /**
     * TODO CJ: write javadoc
     * 
     * @param message
     * @return
     */
    public static String getSessionID(Message message) {
        PacketExtension extension = message.getExtension(
            ActivitiesPacketExtension.ELEMENT, PacketExtensions.NAMESPACE);
        if (extension != null) {
            return ((ActivitiesPacketExtension) extension).getSessionID();
        }
        extension = message.getExtension(RequestPacketExtension.ELEMENT,
            PacketExtensions.NAMESPACE);
        if (extension != null) {
            return ((RequestPacketExtension) extension).getSessionID();
        }
        extension = message.getExtension(PacketExtensions.NAMESPACE);
        return ((DefaultPacketExtension) extension).getValue(SESSION_ID);
    }

    /**
     * @return PacketFilter that only accepts Messages (!) which belong to the
     *         current session
     */
    public static PacketFilter getSessionIDPacketFilter() {

        return new AndFilter(new MessageTypeFilter(Message.Type.chat),
            new PacketFilter() {
                public boolean accept(Packet arg0) {
                    Message message = (Message) arg0;

                    return Saros.getDefault().getSessionManager()
                        .getSessionID().equals(
                            PacketExtensions.getSessionID(message));
                }
            });
    }

    public static MutablePicoContainer container;

    public static synchronized PicoContainer getContainer() {

        if (container == null) {

            container = new PicoBuilder(new CompositeInjection(
                new ConstructorInjection(), new AnnotatedFieldInjection()))
                .withCaching().build();

            container.addComponent(CancelInviteExtension.class).addComponent(
                ChecksumErrorExtension.class).addComponent(
                ChecksumExtension.class).addComponent(
                DataTransferExtension.class)
                .addComponent(InviteExtension.class).addComponent(
                    JoinExtension.class).addComponent(
                    JupiterErrorExtension.class).addComponent(
                    LeaveExtension.class).addComponent(
                    RequestActivityExtension.class).addComponent(
                    RequestForFileListExtension.class).addComponent(
                    UserListExtension.class);
        }
        return container;
    }

    public static PacketFilter getInSessionFilter() {
        return new PacketFilter() {
            public boolean accept(Packet arg0) {
                return Saros.getDefault().getSessionManager()
                    .getSharedProject() != null;
            }
        };
    }
}
