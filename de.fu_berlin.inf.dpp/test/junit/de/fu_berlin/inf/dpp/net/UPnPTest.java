package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;

/**
 * Test class to perform tests on UPnP management functionality in Saros.
 */
public class UPnPTest {

    /**
     * UPnP access stub for UPnPManager to work with to use local test data
     * instead of a real network and gateway.
     */
    protected static class UPnPAccessStub implements IUPnPAccess {
        Collection<GatewayDevice> gatewaysToServe = new ArrayList<GatewayDevice>();
        Map<Integer, PortMappingEntry> portmappings = new HashMap<Integer, PortMappingEntry>();

        /**
         * Adds a {@link GatewayDevice} object to the list of gateways to be
         * discovered.
         */
        protected void addGatewayDevice(GatewayDevice gateway) {
            gatewaysToServe.add(gateway);
        }

        public Collection<GatewayDevice> performDiscovery()
            throws SocketException, UnknownHostException, IOException,
            SAXException, ParserConfigurationException {
            return gatewaysToServe;
        }

        public int deletePortMapping(GatewayDevice gateway, int port,
            String protocol) throws IOException, SAXException {

            return portmappings.remove(port) != null ? 0 : 404;
        }

        public PortMappingEntry getSpecificPortMappingEntry(
            GatewayDevice gateway, int port, String protocol)
            throws IOException, SAXException {
            return portmappings.get(port);
        }

        public int addPortMapping(GatewayDevice gateway, int externalPort,
            int internalPort, String internalClient, String protocol,
            String description, int leaseDuration) throws IOException,
            SAXException {

            PortMappingEntry pmEntry = new PortMappingEntry();
            pmEntry.setExternalPort(externalPort);
            pmEntry.setInternalPort(internalPort);
            pmEntry.setInternalClient(internalClient);
            pmEntry.setProtocol(protocol);
            pmEntry.setLeaseDuration(leaseDuration);

            portmappings.put(internalPort, pmEntry);
            return 0;
        }
    }

    protected UPnPAccessStub upnpAccess;
    protected IUPnPService testUpnpManager;
    protected GatewayDevice testGateway1;
    protected GatewayDevice testGateway2;
    protected GatewayDevice testGateway3;

    @Before
    public void setUp() throws Exception {
        upnpAccess = new UPnPAccessStub();
        testUpnpManager = new UPnPServiceImpl();
        testUpnpManager.init(upnpAccess, null);

        testGateway1 = new GatewayDevice();
        testGateway1.setFriendlyName("Test Gateway 1");
        testGateway1.setUSN("{Test-Gateway:001");
        testGateway1.setLocalAddress(InetAddress.getLocalHost());

        testGateway2 = new GatewayDevice();
        testGateway2.setFriendlyName("Test Gateway 2");
        testGateway2.setUSN("{Test-Gateway:002");
        testGateway2.setLocalAddress(InetAddress.getLocalHost());

        testGateway3 = new GatewayDevice();
        testGateway3.setFriendlyName("Test Gateway 3");
        testGateway3.setUSN("{Test-Gateway:003");
        testGateway3.setLocalAddress(InetAddress.getLocalHost());
    }

    @Test
    public void testIntitialState() {
        assertTrue(testUpnpManager.getGateways() == null);
        assertFalse(testUpnpManager.isMapped());
    }

    @Test
    public void testDiscoveryNoDeviceFound() {
        testUpnpManager.discoverGateways();
        assertTrue(testUpnpManager.getGateways().isEmpty());
        assertFalse(testUpnpManager.isMapped());
    }

    @Test
    public void testDiscovery() {

        // prepare
        upnpAccess.addGatewayDevice(testGateway1);
        upnpAccess.addGatewayDevice(testGateway2);
        upnpAccess.addGatewayDevice(testGateway3);

        // test
        testUpnpManager.discoverGateways();

        // check
        assertEquals(testUpnpManager.getGateways().size(), 3);
        assertTrue(testUpnpManager.getGateways().get(0).equals(testGateway1));
        assertTrue(testUpnpManager.getGateways().get(1).equals(testGateway2));
        assertTrue(testUpnpManager.getGateways().get(2).equals(testGateway3));
    }

    @Test
    public void testPreselectionWithDiscovery() {

        // prepare
        upnpAccess.addGatewayDevice(testGateway1);
        upnpAccess.addGatewayDevice(testGateway2);
        upnpAccess.addGatewayDevice(testGateway3);

        testUpnpManager.setPreSelectedDeviceID(testGateway2.getUSN());

        // test
        testUpnpManager.discoverGateways();

        // check
        assertTrue(testUpnpManager.getSelectedGateway() == testGateway2);
    }

    @Test
    public void testAddAndRemoveSarosPortmapping() {

        // prepare staged environment
        upnpAccess.addGatewayDevice(testGateway1);
        SmackConfiguration.setLocalSocks5ProxyEnabled(true);
        SmackConfiguration.setLocalSocks5ProxyPort(0);
        Socks5Proxy.getSocks5Proxy().start();

        testUpnpManager.setPreSelectedDeviceID(testGateway1.getUSN());
        testUpnpManager.discoverGateways();

        // test adding portmapping
        assertTrue(testUpnpManager.createSarosPortMapping());

        // check
        assertTrue(testUpnpManager.isMapped());
        assertEquals(Socks5Proxy.getSocks5Proxy().getPort(),
            testUpnpManager.getCurrentlyMappedPort());

        // test removing portmapping
        assertTrue(testUpnpManager.removeSarosPortMapping());

        // check
        assertFalse(testUpnpManager.isMapped());

        Socks5Proxy.getSocks5Proxy().stop();
    }
}
