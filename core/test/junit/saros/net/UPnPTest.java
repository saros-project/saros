package saros.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import saros.net.upnp.IGatewayFinder;
import saros.net.upnp.IGateway;
import saros.net.upnp.IUPnPService;
import saros.net.upnp.internal.UPnPServiceImpl;
import java.util.List;
import java.util.ArrayList;
import java.net.InetAddress;


public class UPnPTest {
    private static class GatewayStub implements IGateway {
	String usn;
	String friendlyName;
	String ip;
	InetAddress localAddress;
	InetAddress deviceAddress;

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result
		+ ((usn == null) ? 0 : usn.hashCode());
	    result = prime * result
		+ ((friendlyName == null) ? 0 : friendlyName.hashCode());
	    result = prime * result
		+ ((localAddress == null) ? 0 : localAddress.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    GatewayStub other = (GatewayStub) obj;
	    if (usn == null) {
		if (other.usn != null)
		    return false;
	    } else if (!usn.equals(other.usn))
		return false;
	    if (friendlyName == null) {
		if (other.friendlyName != null)
		    return false;
	    } else if (!friendlyName.equals(other.friendlyName))
		return false;
	    if (localAddress == null) {
		if (other.localAddress != null)
		    return false;
	    } else if (!localAddress.equals(other.localAddress))
		return false;
	    return true;
	}

	@Override
	public String getUSN() {
	    return usn;
	}

	public void setUSN(String usn) {
	    this.usn = usn;
	}

	@Override
	public String getFriendlyName() {
	    return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
	    this.friendlyName = friendlyName;
	}

	@Override
	public String getExternalIPAddress() {
	    return ip;
	}

	@Override
	public InetAddress getLocalAddress() {
	    return localAddress;
	}

	public void setLocalAddress(InetAddress localAddress) {
	    this.localAddress = localAddress;
	}

	@Override
	public InetAddress getDeviceAddress() {
	    return deviceAddress;
	}

	@Override
	public boolean isConnected() {
	    return true;
	}

	@Override
	public boolean isMapped(int port, String protocol) {
	    return false;
	}

	@Override
	public boolean closePort(int port, String protocol) {
	    return true;
	}

	@Override
	public int openPort(int port, String protocol, int LEASE_DURATION, String description) {
	    return 0;
	}
    }

    private static class GatewayFinderStub implements IGatewayFinder {
	List<IGateway> gatewaysToServe = new ArrayList<IGateway>();

	protected void addGatewayDevice(IGateway gateway) {
	    gatewaysToServe.add(gateway);
	}

	@Override
	public List<IGateway> discoverGateways() {
	    return gatewaysToServe;
	}
    }

    private GatewayFinderStub finder;
    private UPnPServiceImpl testUpnpManager;
    private GatewayStub testGateway1;
    private GatewayStub testGateway2;
    private GatewayStub testGateway3;

    @Before
    public void setUp() throws Exception {
	finder = new GatewayFinderStub();
	testUpnpManager = new UPnPServiceImpl(finder);

	testGateway1 = new GatewayStub();
	testGateway1.setFriendlyName("Test Gateway 1");
	testGateway1.setUSN("{Test-Gateway:001}");
	testGateway1.setLocalAddress(InetAddress.getLocalHost());

	testGateway2 = new GatewayStub();
	testGateway2.setFriendlyName("Test Gateway 2");
	testGateway2.setUSN("{Test-Gateway:002}");
	testGateway2.setLocalAddress(InetAddress.getLocalHost());

	testGateway3 = new GatewayStub();
	testGateway3.setFriendlyName("Test Gateway 3");
	testGateway3.setUSN("{Test-Gateway:003}");
	testGateway3.setLocalAddress(InetAddress.getLocalHost());
    }

    @Test
    public void testDiscoveryNoDeviceFound() {
	assertTrue(testUpnpManager.getGateways(true).isEmpty());
    }

    @Test
    public void testDiscovery() {

	finder.addGatewayDevice(testGateway1);
	finder.addGatewayDevice(testGateway2);
	finder.addGatewayDevice(testGateway3);

	assertEquals(testUpnpManager.getGateways(false).size(), 3);
	assertEquals(testUpnpManager.getGateways(false).get(0), testGateway1);
	assertEquals(testUpnpManager.getGateways(false).get(1), testGateway2);
	assertEquals(testUpnpManager.getGateways(false).get(2), testGateway3);
    }

    @Test
    public void testAddAndRemovePortmapping() {

	final int port = 4711;

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
