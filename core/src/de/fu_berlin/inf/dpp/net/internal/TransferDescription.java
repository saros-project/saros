/** */
package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * A transfer description contains all necessary information for tunneling packet extension through
 * a plain TCP connection. <b>Note:</b> Modifying this class (e.g adding fields) requires changes in
 * the {@link BinaryChannelConnection} class !
 */
public class TransferDescription {

  private TransferDescription() {
    // NOP
  }

  private String elementName;

  private String namespace;

  private JID recipient;

  private JID sender;

  /** Field used to indicate that the payload may be compressed. */
  private boolean compress;

  public static TransferDescription newDescription() {
    return new TransferDescription();
  }

  TransferDescription setNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  TransferDescription setElementName(String elementName) {
    this.elementName = elementName;
    return this;
  }

  public String getElementName() {
    return elementName;
  }

  TransferDescription setRecipient(JID recipient) {
    this.recipient = recipient;
    return this;
  }

  public JID getRecipient() {
    return recipient;
  }

  TransferDescription setSender(JID sender) {
    this.sender = sender;
    return this;
  }

  public JID getSender() {
    return sender;
  }

  TransferDescription setCompressContent(boolean compress) {
    this.compress = compress;
    return this;
  }

  public boolean compressContent() {
    return compress;
  }

  @Override
  public String toString() {
    return "TransferDescription [elementName="
        + elementName
        + ", namespace="
        + namespace
        + ", recipient="
        + recipient
        + ", sender="
        + sender
        + ", compress="
        + compress
        + "]";
  }
}
