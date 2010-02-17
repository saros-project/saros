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

import java.io.IOException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.inf.dpp.net.internal.extensions.DropSilentlyPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;
import de.fu_berlin.inf.dpp.util.xstream.XppReader;

/**
 * Flexible extension provider using XStream to serialize arbitrary data
 * objects.
 * 
 * Supports PacketExtension and IQPackets
 */
public class XStreamExtensionProvider<T> implements PacketExtensionProvider,
    IQProvider {

    private static final Logger log = Logger
        .getLogger(XStreamExtensionProvider.class);

    protected String namespace;

    protected String elementName;

    protected XStream xstream;

    /**
     * Create a new XStreamExtensionProvider using the given elementName as the
     * XML root element. The Provider is able to understand the given classes,
     * which should be annotated using XStream annotations.
     */
    public XStreamExtensionProvider(String elementName, Class<?>... classes) {
        this.elementName = elementName;
        this.namespace = PacketExtensionUtils.NAMESPACE;

        xstream = new XStream();
        xstream.registerConverter(new IPathConverter());
        xstream.processAnnotations(XStreamPacketExtension.class);
        xstream.processAnnotations(classes);
        xstream.alias(elementName, XStreamPacketExtension.class);

        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addExtensionProvider(getElementName(), getNamespace(),
            this);
        providerManager.addIQProvider(getElementName(), getNamespace(), this);

        // TODO Validate that elementName is a valid XML identifier
    }

    public static class XStreamIQPacket<T> extends IQ {

        protected XStreamPacketExtension<T> child;

        protected XStreamIQPacket(XStreamPacketExtension<T> child) {
            if (child == null)
                throw new IllegalArgumentException("Child must be given!");
            this.child = child;
        }

        /**
         * Returns whether this IQPacket is compatible with the given provider.
         */
        public boolean accept(XStreamExtensionProvider<?> provider) {
            return child.accept(provider);
        }

        @Override
        public String getChildElementXML() {
            return child.toXML();
        }

        public T getPayload() {
            return child.getPayload();
        }
    }

    public static class XStreamPacketExtension<T> implements PacketExtension {

        /**
         * Necessary for Smack
         */
        @XStreamAsAttribute
        protected String xmlns;

        protected T payload;

        @XStreamOmitField
        protected XStreamExtensionProvider<T> provider;

        protected XStreamPacketExtension(
            XStreamExtensionProvider<T> ourProvider, T payload) {
            this.xmlns = ourProvider.getNamespace();
            this.payload = payload;
            this.provider = ourProvider;
        }

        /**
         * Returns whether this XStreamPacketExtension is compatible with the
         * given provider
         */
        public boolean accept(XStreamExtensionProvider<?> provider) {
            return ObjectUtils.equals(getElementName(), provider
                .getElementName())
                && ObjectUtils.equals(getNamespace(), provider.getNamespace());
        }

        public String getElementName() {
            return provider.getElementName();
        }

        public T getPayload() {
            return payload;
        }

        public String getNamespace() {
            return provider.getNamespace();
        }

        public String toXML() {
            return provider.xstream.toXML(this);
        }
    }

    /**
     * PacketFilter for Packets which contain an PacketExtension matching the
     * {@link XStreamExtensionProvider#elementName} and
     * {@link PacketExtensionUtils#NAMESPACE}.
     */
    public PacketFilter getPacketFilter() {
        return new PacketExtensionFilter(getElementName(), getNamespace());
    }

    public String getNamespace() {
        return namespace;
    }

    public String getElementName() {
        return elementName;
    }

    public PacketFilter getIQFilter() {
        return new PacketFilter() {
            public boolean accept(Packet packet) {
                if (!(packet instanceof XStreamIQPacket<?>))
                    return false;

                return ((XStreamIQPacket<?>) packet)
                    .accept(XStreamExtensionProvider.this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public PacketExtension parseExtension(XmlPullParser parser) {
        try {
            XStreamPacketExtension<T> result = (XStreamPacketExtension<T>) xstream
                .unmarshal(new XppReader(parser));
            result.provider = this;
            return result;
        } catch (RuntimeException e) {
            log.error("Malformed data received!", e);
            return new DropSilentlyPacketExtension();
        }
    }

    /**
     * Returns the payload transported in this packet for this extensions
     * provider.
     * 
     * This method can handle IQ and PacketExtensions used for transferring
     * payloads.
     * 
     * If the packet contains no matching data (or if the packet is null), null
     * is returned.
     * 
     * @throws ClassCastException
     *             if somebody has registered an PacketExtension under our
     *             {@link XStreamExtensionProvider#elementName}
     */
    @SuppressWarnings("unchecked")
    public T getPayload(Packet packet) {

        if (packet == null)
            return null;

        // First check whether this is one of our IQ Packets
        if (packet instanceof XStreamIQPacket
            && ((XStreamIQPacket<T>) packet).accept(this)) {
            return ((XStreamIQPacket<T>) packet).getPayload();
        }

        // Otherwise check if this packets contains an extension we support
        return getPayload(packet.getExtension(getElementName(), getNamespace()));
    }

    @SuppressWarnings("unchecked")
    public T getPayload(PacketExtension extension) {

        if (extension == null)
            return null;

        if (extension instanceof XStreamPacketExtension<?>
            && ((XStreamPacketExtension<?>) extension).accept(this)) {
            return ((XStreamPacketExtension<T>) extension).getPayload();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T parseString(String string) throws IOException {
        try {
            return ((XStreamPacketExtension<T>) xstream.fromXML(string))
                .getPayload();
        } catch (Exception e) {
            throw new CausedIOException(e);
        }
    }

    public XStreamPacketExtension<T> create(T t) {
        return new XStreamPacketExtension<T>(this, t);
    }

    public IQ createIQ(T t) {
        return new XStreamIQPacket<T>(create(t));
    }

    @SuppressWarnings("unchecked")
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        try {
            XStreamPacketExtension<T> result = (XStreamPacketExtension<T>) xstream
                .unmarshal(new XppReader(parser));
            result.provider = this;
            return new XStreamIQPacket<T>(result);
        } catch (RuntimeException e) {
            log.error("Malformed data received!", e);
            return null;
        }

    }
}
