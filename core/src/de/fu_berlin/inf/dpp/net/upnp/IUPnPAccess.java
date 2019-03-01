package de.fu_berlin.inf.dpp.net.upnp;

import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

/**
 * An interface responsible for UPnP accessing functionality. Implementing classes can be used for
 * the {@link UPnPServiceImpl} to perform UPnP actions with.
 */
public interface IUPnPAccess {
  Collection<GatewayDevice> performDiscovery()
      throws SocketException, UnknownHostException, IOException, SAXException,
          ParserConfigurationException;

  /**
   * Deletes the specified port mapping associated with the external port and the protocol.
   *
   * @param gateway A UPnP-compliant gateway device using the mapping.
   * @param port The external port
   * @param protocol The protocol
   * @return Error code. 0 if successful.
   * @throws IOException
   * @throws SAXException
   */
  int deletePortMapping(GatewayDevice gateway, int port, String protocol)
      throws IOException, SAXException;

  PortMappingEntry getSpecificPortMappingEntry(GatewayDevice gateway, int port, String protocol)
      throws IOException, SAXException;

  /**
   * Adds a new port mapping to a gateway device.
   *
   * @param gateway The gateway device receiving the new mapping.
   * @param externalPort The external port associated with the new mapping
   * @param internalPort The internal port associated with the new mapping
   * @param internalClient The internal client associated with the new mapping
   * @param protocol The protocol associated with the new mapping
   * @param description A description to accompany the mapping
   * @param leaseDuration amount of seconds this mapping is valid. Use 0 for unlimited duration.
   * @return Error code. 0 if successful.
   * @throws IOException
   * @throws SAXException
   */
  int addPortMapping(
      GatewayDevice gateway,
      int externalPort,
      int internalPort,
      String internalClient,
      String protocol,
      String description,
      int leaseDuration)
      throws IOException, SAXException;
}
