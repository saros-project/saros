package de.fu_berlin.inf.dpp.net.upnp.internal;

import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

/** UPnP access proxy for Saros to access the UPnP library weupnp. */
public class UPnPAccessImpl implements IUPnPAccess {

  @Override
  public Collection<GatewayDevice> performDiscovery()
      throws SocketException, UnknownHostException, IOException, SAXException,
          ParserConfigurationException {
    return new GatewayDiscover().discover();
  }

  @Override
  public int deletePortMapping(GatewayDevice gateway, int port, String protocol)
      throws IOException, SAXException {

    return gateway.deletePortMapping(port, protocol);
  }

  @Override
  public PortMappingEntry getSpecificPortMappingEntry(
      GatewayDevice gateway, int port, String protocol) throws IOException, SAXException {

    return gateway.getSpecificPortMappingEntry(port, protocol);
  }

  @Override
  public int addPortMapping(
      GatewayDevice gateway,
      int externalPort,
      int internalPort,
      String internalClient,
      String protocol,
      String description,
      int leaseDuration)
      throws IOException, SAXException {

    return gateway.addPortMapping(
        externalPort, internalPort, internalClient, protocol, description, leaseDuration);
  }
}
