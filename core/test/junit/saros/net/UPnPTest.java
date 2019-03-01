package saros.net;

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
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import saros.net.upnp.IUPnPAccess;
import saros.net.upnp.IUPnPService;
import saros.net.upnp.internal.UPnPServiceImpl;

/** Test class to perform tests on UPnP management functionality in Saros. */
public class UPnPTest {

  /**
   * UPnP access stub for UPnPManager to work with to use local test data instead of a real network
   * and gateway.
   */
  private static class UPnPAccessStub implements IUPnPAccess {
    Collection<GatewayDevice> gatewaysToServe = new ArrayList<GatewayDevice>();
    Map<Integer, PortMappingEntry> portmappings = new HashMap<Integer, PortMappingEntry>();

    /** Adds a {@link GatewayDevice} object to the list of gateways to be discovered. */
    protected void addGatewayDevice(GatewayDevice gateway) {
      gatewaysToServe.add(gateway);
    }

    @Override
    public Collection<GatewayDevice> performDiscovery()
        throws SocketException, UnknownHostException, IOException, SAXException,
            ParserConfigurationException {
      return gatewaysToServe;
    }

    @Override
    public int deletePortMapping(GatewayDevice gateway, int port, String protocol)
        throws IOException, SAXException {

      return portmappings.remove(port) != null ? 0 : 404;
    }

    @Override
    public PortMappingEntry getSpecificPortMappingEntry(
        GatewayDevice gateway, int port, String protocol) throws IOException, SAXException {
      return portmappings.get(port);
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

  private UPnPAccessStub upnpAccess;
  private UPnPServiceImpl testUpnpManager;
  private GatewayDevice testGateway1;
  private GatewayDevice testGateway2;
  private GatewayDevice testGateway3;

  @Before
  public void setUp() throws Exception {
    upnpAccess = new UPnPAccessStub();
    testUpnpManager = new UPnPServiceImpl(upnpAccess);

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
  public void testDiscoveryNoDeviceFound() {
    assertTrue(testUpnpManager.getGateways(true).isEmpty());
  }

  @Test
  public void testDiscovery() {

    upnpAccess.addGatewayDevice(testGateway1);
    upnpAccess.addGatewayDevice(testGateway2);
    upnpAccess.addGatewayDevice(testGateway3);

    assertEquals(testUpnpManager.getGateways(false).size(), 3);
    assertEquals(testUpnpManager.getGateways(false).get(0), testGateway1);
    assertEquals(testUpnpManager.getGateways(false).get(1), testGateway2);
    assertEquals(testUpnpManager.getGateways(false).get(2), testGateway3);
  }

  @Test
  public void testAddAndRemovePortmapping() {

    final int port = 4711;

    upnpAccess.addGatewayDevice(testGateway1);

    assertTrue(
        "failed to create port mapping",
        testUpnpManager.createPortMapping(testGateway1, port, IUPnPService.TCP, null));

    assertTrue(
        "internal port mapping entries were not updated",
        testUpnpManager.isMapped(testGateway1, port, IUPnPService.TCP));

    assertTrue(
        "failed to remove port mapping",
        testUpnpManager.deletePortMapping(testGateway1, port, IUPnPService.TCP));

    assertFalse(
        "internal port mapping entries were not updated",
        testUpnpManager.isMapped(testGateway1, port, IUPnPService.TCP));
  }

  @Test
  public void testDispose() {

    upnpAccess.addGatewayDevice(testGateway1);

    testUpnpManager.createPortMapping(testGateway1, 4711, IUPnPService.TCP, null);

    testUpnpManager.createPortMapping(testGateway1, 1174, IUPnPService.UDP, null);

    testUpnpManager.dispose();

    assertFalse(
        "internal port mapping were not updated on dispose",
        testUpnpManager.isMapped(testGateway1, 4711, IUPnPService.TCP));

    assertFalse(
        "internal port mapping were not updated on dispose",
        testUpnpManager.isMapped(testGateway1, 4711, IUPnPService.UDP));
  }
}
