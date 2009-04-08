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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.PacketListener;
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
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;
import de.fu_berlin.inf.dpp.project.ISharedProject;

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

    /**
     * A Saros Packet Extension is responsible for converting between the
     * network component (XMPPChatTransmitter) and the business logic
     */
    public static abstract class SarosPacketExtension implements PacketListener {

        public SarosPacketExtension(String element) {
            this.element = element;
        }

        String element;

        /**
         * Dispatch all Packets that pass the filter to the processMessage
         * method, because we always work with Messages.
         */
        public void processPacket(Packet packet) {
            if (!getFilter().accept(packet))
                return;

            processMessage(new JID(packet.getFrom()), (Message) packet);
        }

        /**
         * Every subclass that represents a PackageExtension is supposed to
         * implement this method by unpacking the data in message an calling a
         * method that subclasses in PacketExtensions can implement.
         */
        public abstract void processMessage(JID sender, Message message);

        public PacketFilter getFilter() {
            return new PacketExtensionFilter(element, NAMESPACE);
        }

        public boolean hasExtension(Message m) {
            return m.getExtension(element, NAMESPACE) != null;
        }
    }

    public static abstract class SarosDefaultPacketExtension extends
        SarosPacketExtension {

        public SarosDefaultPacketExtension(String element) {
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
        SarosDefaultPacketExtension {

        public SessionDefaultPacketExtension(String element) {
            super(element);
        }

        @Override
        public DefaultPacketExtension create() {
            DefaultPacketExtension extension = super.create();

            extension.setValue(PacketExtensions.SESSION_ID, getSessionID());

            return extension;
        }
    }

    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    // attributes
    public static final String SESSION_ID = "sessionID";

    public static final String DESCRIPTION = "description";

    public static final String PROJECTNAME = "description";

    public static final String COLOR_ID = "ColorID";

    public static void hookExtensionProviders() {

        ProviderManager providermanager = ProviderManager.getInstance();
        providermanager.addExtensionProvider(ActivitiesPacketExtension.ELEMENT,
            PacketExtensions.NAMESPACE, new ActivitiesExtensionProvider());
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
     * @return A PacketFilter that only accepts Packets if there is currently a
     *         SharedProject
     */
    public static PacketFilter getInSessionFilter() {
        return new PacketFilter() {
            public boolean accept(Packet arg0) {
                return Saros.getDefault().getSessionManager()
                    .getSharedProject() != null;
            }
        };
    }

    /**
     * @return filter that returns true iff currently a shared project exists
     *         and the message was from the host of this shared project.
     */
    public static PacketFilter getFromHostFilter() {
        return new PacketFilter() {
            public boolean accept(Packet packet) {
                ISharedProject project = Saros.getDefault().getSessionManager()
                    .getSharedProject();

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

    protected static MutablePicoContainer container;

    /**
     * Returns a PicoContainer which contains default implementation of all
     * PacketExtensions, which do nothing in their callback function
     */
    public static synchronized PicoContainer getContainer() {

        if (container == null) {

            container = new PicoBuilder(new CompositeInjection(
                new ConstructorInjection(), new AnnotatedFieldInjection()))
                .withCaching().build();

            container.addComponent(CancelInviteExtension.class,
                new CancelInviteExtension() {

                    @Override
                    public void invitationCanceledReceived(JID sender,
                        String errorMessage) {
                        // Default implementation does nothing
                    }
                });
            container.addComponent(ChecksumErrorExtension.class,
                new ChecksumErrorExtension() {

                    @Override
                    public void checksumErrorReceived(JID sender,
                        Set<IPath> paths, boolean resolved) {
                        // Default implementation does nothing
                    }

                });

            container.addComponent(ChecksumExtension.class,
                new ChecksumExtension() {

                    @Override
                    public void checksumsReceived(JID sender,
                        List<DocumentChecksum> checksums) {
                        // Default implementation does nothing
                    }

                });

            container.addComponent(DataTransferExtension.class,
                new DataTransferExtension() {

                    @Override
                    public void chunkReceived(JID sender, String name,
                        String desc, int index, int maxIndex, String data) {
                        // Default implementation does nothing
                    }
                });

            container.addComponent(InviteExtension.class,
                new InviteExtension() {

                    @Override
                    public void invitationReceived(JID sender,
                        String sessionID, String projectName,
                        String description, int colorID) {
                        // Default implementation does nothing
                    }
                });
            container.addComponent(JoinExtension.class, new JoinExtension() {

                @Override
                public void joinReceived(JID sender, int colorID) {
                    // Default implementation does nothing
                }
            });
            container.addComponent(LeaveExtension.class, new LeaveExtension() {

                @Override
                public void leaveReceived(JID sender) {
                    // Default implementation does nothing
                }
            });
            container.addComponent(RequestActivityExtension.class,
                new RequestActivityExtension() {

                    @Override
                    public void requestForResendingActivitiesReceived(
                        JID sender, int timeStamp, boolean andUp) {
                        // Default implementation does nothing
                    }
                });
            container.addComponent(RequestForFileListExtension.class,
                new RequestForFileListExtension() {

                    @Override
                    public void requestForFileListReceived(JID sender) {
                        // Default implementation does nothing
                    }
                });
            container.addComponent(UserListExtension.class,
                new UserListExtension() {

                    @Override
                    public void userListReceived(JID sender, List<User> userList) {
                        // Default implementation does nothing
                    }
                });
        }
        return container;
    }
}
