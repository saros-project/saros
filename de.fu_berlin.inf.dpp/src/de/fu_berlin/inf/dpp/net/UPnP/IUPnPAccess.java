package de.fu_berlin.inf.dpp.net.UPnP;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

/**
 * An interface responsible for UPnP accessing functionality. Implementing
 * classes can be used for the {@link UPnPManager} to perform UPnP actions with.
 */
public interface IUPnPAccess {
    Collection<GatewayDevice> performDiscovery() throws SocketException,
        UnknownHostException, IOException, SAXException,
        ParserConfigurationException;

    int deletePortMapping(GatewayDevice gateway, int port, String protocol)
        throws IOException, SAXException;

    PortMappingEntry getSpecificPortMappingEntry(GatewayDevice gateway,
        int port, String protocol) throws IOException, SAXException;

    int addPortMapping(GatewayDevice gateway, int externalPort,
        int internalPort, String internalClient, String protocol,
        String description, int leaseDuration) throws IOException, SAXException;

}
