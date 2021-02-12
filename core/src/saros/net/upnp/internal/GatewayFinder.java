package saros.net.upnp.internal;

import saros.net.upnp.IGatewayFinder;
import saros.net.upnp.IGateway;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;


public class GatewayFinder implements IGatewayFinder{

    private static final String[] SEARCH_MESSAGES;

    static {
        LinkedList<String> m = new LinkedList<String>();
        for (String type : new String[]{"urn:schemas-upnp-org:device:InternetGatewayDevice:1", "urn:schemas-upnp-org:service:WANIPConnection:1", "urn:schemas-upnp-org:service:WANPPPConnection:1"}) {
            m.add("M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: " + type + "\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n");
        }
        SEARCH_MESSAGES = m.toArray(new String[]{});
    }
    private List<IGateway> devices = new ArrayList<IGateway>();

    private class GatewayListener extends Thread {

        private Inet4Address ip;
        private String req;

        public GatewayListener(Inet4Address ip, String req) {
            setName("WaifUPnP - Gateway Listener");
            this.ip = ip;
            this.req = req;
        }

        @Override
        public void run() {
            DatagramSocket s = null;
            try {
                byte[] req = this.req.getBytes();
                s = new DatagramSocket(new InetSocketAddress(ip, 0));
                s.send(new DatagramPacket(req, req.length, new InetSocketAddress("239.255.255.250", 1900)));
                s.setSoTimeout(3000);
                for (;;) {
                    try {
                        DatagramPacket recv = new DatagramPacket(new byte[1536], 1536);
                        s.receive(recv);
                        InetAddress devAddr = recv.getAddress();
                        Gateway gw = new Gateway(recv.getData());
                        gw.setLocalAddress(ip);
                        gw.setDeviceAddress(devAddr);
                        synchronized (devices) {
                            if (!devices.contains(gw)) {
                                devices.add(gw);
                                break;
                            }
                        }
                    } catch (SocketTimeoutException t) {
                        break;
                    } catch (Throwable t) {
                    }
                }
            } catch (Throwable t) { }
            finally {
                s.close();
            }
        }
    }

    @Override
    public List<IGateway> discoverGateways() {
        LinkedList<GatewayListener> listeners = new LinkedList<GatewayListener>();
        for (String req : SEARCH_MESSAGES) {
            for (Inet4Address ip : getLocalIPs()) {
                GatewayListener l = new GatewayListener(ip, req);
                l.start();
                listeners.add(l);
            }
            for (GatewayListener l : listeners) {
                try {
                    l.join();
                } catch (InterruptedException e) {}
            }
            if (!devices.isEmpty())
                break;
        }
        return devices;
    }

    private static Inet4Address[] getLocalIPs() {
        LinkedList<Inet4Address> ret = new LinkedList<Inet4Address>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                try {
                    NetworkInterface iface = ifaces.nextElement();
                    if (!iface.isUp() || iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint()) {
                        continue;
                    }
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    if (addrs == null) {
                        continue;
                    }
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        if (addr instanceof Inet4Address) {
                            ret.add((Inet4Address) addr);
                        }
                    }
                } catch (Throwable t) {
                }
            }
        } catch (Throwable t) {
        }
        return ret.toArray(new Inet4Address[]{});
    }

}
