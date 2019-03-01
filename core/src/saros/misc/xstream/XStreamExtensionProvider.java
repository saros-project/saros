/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package saros.misc.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
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

/**
 * Flexible extension provider using XStream to serialize arbitrary data objects.
 *
 * <p>Supports PacketExtension and IQPackets
 */
public class XStreamExtensionProvider<T> implements PacketExtensionProvider, IQProvider {

  private static final Logger LOG = Logger.getLogger(XStreamExtensionProvider.class);

  private static volatile ClassLoader currentClassloader;

  protected final String namespace;

  protected final String elementName;

  private final XStream xstream;

  private Map<Class<? extends Converter>, ReplaceableConverter> replaceables;
  private Map<Class<? extends SingleValueConverter>, ReplaceableSingleValueConverter>
      replaceableSingles;

  /**
   * Sets the class loader to use when a new provider is created. This class loader will be used by
   * {@link XStream} to unmarshal the given packet extension.
   *
   * @param classLoader the class loader to use or <code>null</code> to use the class loader the
   *     provider was loaded with
   * @see #XStreamExtensionProvider(String, String, Class...)
   */
  public static void setClassLoader(ClassLoader classLoader) {
    currentClassloader = classLoader;
  }

  /**
   * Create a new XStreamExtensionProvider using the given element name as the XML root element with
   * the given namespace. The Provider is able to understand the given classes, which should be
   * annotated using XStream annotations.
   *
   * <p><b>Important</b>: use valid XML element names and namespaces or the receiving side will be
   * unable to decode the extension !
   */
  public XStreamExtensionProvider(String namespace, String elementName, Class<?>... classes) {

    if (namespace == null) throw new NullPointerException("namespace is null");

    ClassLoader classLoader = currentClassloader;

    this.elementName = elementName;
    this.namespace = namespace;

    xstream = new XStream();

    if (classLoader != null) xstream.setClassLoader(classLoader);
    else xstream.setClassLoader(getClass().getClassLoader());

    xstream.registerConverter(BooleanConverter.BINARY);
    xstream.registerConverter(new UrlEncodingStringConverter());
    xstream.processAnnotations(XStreamPacketExtension.class);
    xstream.processAnnotations(classes);
    xstream.alias(elementName, XStreamPacketExtension.class);

    ProviderManager providerManager = ProviderManager.getInstance();
    providerManager.addExtensionProvider(getElementName(), getNamespace(), this);
    providerManager.addIQProvider(getElementName(), getNamespace(), this);

    // TODO Validate that elementName is a valid XML identifier

    replaceables = new HashMap<Class<? extends Converter>, ReplaceableConverter>();
    replaceableSingles =
        new HashMap<Class<? extends SingleValueConverter>, ReplaceableSingleValueConverter>();
  }

  /**
   * Register additional {@link Converter}s at runtime. This is useful if a converter cannot be used
   * isolatedly, e.g. because it requires a running Saros session.
   *
   * @param converter The {@link Converter} to be registered to XStream. There can only be one
   *     instance per converter class. If a new instance of an already registered class is
   *     registered, the old instance will be replaced.
   */
  public void registerConverter(Converter converter) {
    Class<? extends Converter> clazz = converter.getClass();

    if (replaceables.containsKey(clazz)) {
      LOG.debug("Renewing existing converter of " + clazz);
      replaceables.get(clazz).replace(converter);
      return;
    }

    LOG.debug("Registering new converter of " + clazz);

    ReplaceableConverter replaceable = new ReplaceableConverter(converter);
    xstream.registerConverter(replaceable);
    replaceables.put(clazz, replaceable);
  }

  /**
   * Unregisters a previously registered {@link Converter} from XStream.
   *
   * @param converter If this converter (more precisely: one of the same class) was registered
   *     through {@link #registerConverter(Converter)}, it will no longer be called by XStream.
   *     Otherwise, nothing happens.
   */
  public void unregisterConverter(Converter converter) {
    Class<? extends Converter> clazz = converter.getClass();

    if (replaceables.containsKey(clazz)) {
      LOG.debug("Unregistering (resetting) converter of " + clazz);
      replaceables.get(clazz).reset();
    }
  }

  /**
   * Register additional {@link SingleValueConverter}s at runtime. This is useful if a converter
   * cannot be used isolatedly, e.g. because it requires a running Saros session.
   *
   * @param converter The {@link SingleValueConverter} to be registered to XStream. There can only
   *     be one instance per converter class. If a new instance of an already registered class is
   *     registered, the old instance will be replaced.
   */
  public void registerConverter(SingleValueConverter converter) {
    Class<? extends SingleValueConverter> clazz = converter.getClass();

    if (replaceableSingles.containsKey(clazz)) {
      LOG.debug("Renewing existing converter of " + clazz);
      replaceableSingles.get(clazz).replace(converter);
      return;
    }

    LOG.debug("Registering new converter of " + clazz);

    ReplaceableSingleValueConverter replaceable = new ReplaceableSingleValueConverter(converter);
    xstream.registerConverter(replaceable);
    replaceableSingles.put(clazz, replaceable);
  }

  /**
   * Unregisters a previously registered {@link SingleValueConverter} from XStream.
   *
   * @param converter If this converter (more precisely: one of the same class) was registered
   *     through {@link #registerConverter(SingleValueConverter)}, it will no longer be called by
   *     XStream. Otherwise, nothing happens.
   */
  public void unregisterConverter(SingleValueConverter converter) {
    Class<? extends SingleValueConverter> clazz = converter.getClass();

    if (replaceableSingles.containsKey(clazz)) {
      LOG.debug("Unregistering (resetting) converter of " + clazz);
      replaceableSingles.get(clazz).reset();
    }
  }

  public static class XStreamIQPacket<T> extends IQ {

    protected XStreamPacketExtension<T> child;

    protected XStreamIQPacket(XStreamPacketExtension<T> child) {
      if (child == null) throw new IllegalArgumentException("Child must be given!");
      this.child = child;
    }

    /** Returns whether this IQPacket is compatible with the given provider. */
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

    /** Necessary for Smack */
    @XStreamAsAttribute protected String xmlns;

    protected T payload;

    @XStreamOmitField protected XStreamExtensionProvider<T> provider;

    protected XStreamPacketExtension(XStreamExtensionProvider<T> ourProvider, T payload) {
      this.xmlns = ourProvider.getNamespace();
      this.payload = payload;
      this.provider = ourProvider;
    }

    /** Returns whether this XStreamPacketExtension is compatible with the given provider */
    public boolean accept(XStreamExtensionProvider<?> provider) {
      return ObjectUtils.equals(getElementName(), provider.getElementName())
          && ObjectUtils.equals(getNamespace(), provider.getNamespace());
    }

    @Override
    public String getElementName() {
      return provider.getElementName();
    }

    public T getPayload() {
      return payload;
    }

    @Override
    public String getNamespace() {
      return provider.getNamespace();
    }

    @Override
    public String toXML() {
      StringWriter writer = new StringWriter(512);
      provider.xstream.marshal(this, new CompactWriter(writer));
      return writer.toString();
    }
  }

  /**
   * PacketFilter for Packets which contain a PacketExtension matching the {@link
   * XStreamExtensionProvider#elementName} and {@link XStreamExtensionProvider#namespace}.
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
      @Override
      public boolean accept(Packet packet) {
        if (!(packet instanceof XStreamIQPacket<?>)) return false;

        return ((XStreamIQPacket<?>) packet).accept(XStreamExtensionProvider.this);
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public PacketExtension parseExtension(XmlPullParser parser) {
    try {
      XStreamPacketExtension<T> result =
          (XStreamPacketExtension<T>) xstream.unmarshal(new XppReader(parser));
      result.provider = this;
      return result;
    } catch (RuntimeException e) {
      LOG.error("unmarshalling data failed", e);
      return new DropSilentlyPacketExtension();
    }
  }

  /**
   * Returns the payload transported in this packet for this extensions provider.
   *
   * <p>This method can handle IQ and PacketExtensions used for transferring payloads.
   *
   * <p>If the packet contains no matching data (or if the packet is null), null is returned.
   *
   * @throws ClassCastException if somebody has registered a PacketExtension under our {@link
   *     XStreamExtensionProvider#elementName}
   */
  @SuppressWarnings("unchecked")
  public T getPayload(Packet packet) {

    if (packet == null) return null;

    // First check whether this is one of our IQ Packets
    if (packet instanceof XStreamIQPacket && ((XStreamIQPacket<T>) packet).accept(this)) {
      return ((XStreamIQPacket<T>) packet).getPayload();
    }

    // Otherwise check if this packets contains an extension we support
    return getPayload(packet.getExtension(getElementName(), getNamespace()));
  }

  @SuppressWarnings("unchecked")
  public T getPayload(PacketExtension extension) {

    if (extension == null) return null;

    if (extension instanceof XStreamPacketExtension<?>
        && ((XStreamPacketExtension<?>) extension).accept(this)) {
      return ((XStreamPacketExtension<T>) extension).getPayload();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public T parseString(String string) throws IOException {
    try {
      return ((XStreamPacketExtension<T>) xstream.fromXML(string)).getPayload();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public XStreamPacketExtension<T> create(T t) {
    return new XStreamPacketExtension<T>(this, t);
  }

  public IQ createIQ(T t) {
    return new XStreamIQPacket<T>(create(t));
  }

  @Override
  @SuppressWarnings("unchecked")
  public IQ parseIQ(XmlPullParser parser) throws Exception {
    try {
      XStreamPacketExtension<T> result =
          (XStreamPacketExtension<T>) xstream.unmarshal(new XppReader(parser));
      result.provider = this;
      return new XStreamIQPacket<T>(result);
    } catch (RuntimeException e) {
      LOG.error("unmarshalling data failed", e);
      return null;
    }
  }

  private static class DropSilentlyPacketExtension implements PacketExtension {

    @Override
    public String getElementName() {
      return "drop";
    }

    @Override
    public String getNamespace() {
      return "drop";
    }

    @Override
    public String toXML() {
      StringBuilder buf = new StringBuilder();
      buf.append("<")
          .append(getElementName())
          .append(" xmlns=\"")
          .append(getNamespace())
          .append("\"/>");
      return buf.toString();
    }
  }
}
