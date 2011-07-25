package de.fu_berlin.inf.dpp.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;

import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

public class StunHelper {

    protected static Logger log = Logger.getLogger(StunHelper.class);

    /**
     * Set, if a local IP IS a public IP. (e.g. direct connection)
     */
    protected boolean aLocalIPisPublicIP;

    /**
     * Last known public IP per local IP
     */
    protected Map<InetAddress, InetAddress> recentPublicIPs;

    /**
     * Cache of detected STUN results
     */
    protected Collection<DiscoveryInfo> stunResults;

    public StunHelper() {
        aLocalIPisPublicIP = false;
        recentPublicIPs = new HashMap<InetAddress, InetAddress>();
        stunResults = new ArrayList<DiscoveryInfo>();
    }

    /**
     * Returns whether a local retrieved IP is a public IP.
     * 
     * @return whether a local retrieved IP is a public IP.
     */
    public boolean isLocalIPthePublicIP() {
        return aLocalIPisPublicIP;
    }

    public Collection<DiscoveryInfo> getStunResults() {
        return stunResults;
    }

    synchronized public void setPublicIP(InetAddress privateIP,
        InetAddress publicIP) {
        Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();

        if (recentPublicIPs.containsKey(privateIP)) {
            proxy.removeLocalAddress(recentPublicIPs.get(privateIP)
                .getHostAddress());

            recentPublicIPs.remove(privateIP);
        }

        // log.info("STUN results:\n" + di.toString());

        // add WAN-IP to proxy addresses
        NetworkingUtils.addProxyAddress(publicIP.getHostAddress(), true);
        recentPublicIPs.put(privateIP, publicIP);
    }

    /**
     * Thread class for performing jSTUN discovery to retrieve network
     * information.
     */
    private class StunDiscovery implements Runnable {
        InetAddress localAddress;
        String stunHost;
        int stunPort;

        public StunDiscovery(InetAddress localAddress, String stunHost,
            int stunPort) {
            this.localAddress = localAddress;
            this.stunHost = stunHost;
            this.stunPort = stunPort;
        }

        public void run() {
            try {
                // Perform the jSTUN discovery
                DiscoveryTest disc = new DiscoveryTest(localAddress, stunHost,
                    stunPort);
                DiscoveryInfo di = disc.test();

                InetAddress ip = di.getPublicIP();

                /*
                 * If a public IP was retrieved, add it to the local addresses
                 * of the Socks5 proxy for external clients to be able to
                 * connect to me.
                 */
                Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();
                if (ip != null) {
                    // the local IP matches the retrieved IP address, so it is
                    // already the public IP. Store this fact for later.
                    if (ip.equals(localAddress)) {
                        aLocalIPisPublicIP = true;
                        return;
                    }

                    synchronized (stunResults) {
                        stunResults.add(di);
                    }

                    // Add public IP if its not already in the list
                    if (!proxy.getLocalAddresses()
                        .contains(ip.getHostAddress())) {
                        setPublicIP(localAddress, ip);
                        log.debug("Added WAN-IP: " + ip.getHostAddress()
                            + " (through " + localAddress.getHostAddress()
                            + ")");
                    }
                }

            } catch (Exception e) {
                log.debug("Error while performing STUN: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves the WAN (external) IP of this system using a STUN server by
     * calling jSTUN discovery concurrently.
     * 
     * @param stunAddress
     *            Address of the STUN server
     * @param stunPort
     *            Port of the STUN server
     */
    public void startWANIPDetection(String stunAddress, int stunPort,
        boolean blocking) {

        // Check if the stun server settings are valid, abort otherwise
        if (stunAddress.isEmpty() || stunPort == 0)
            return;

        List<InetAddress> localIPs = null;

        // If we know local IPs to bind
        if (!recentPublicIPs.isEmpty()) {
            localIPs = new LinkedList<InetAddress>(recentPublicIPs.keySet());
        } else {
            try {
                localIPs = NetworkingUtils
                    .getAllNonLoopbackLocalIPAdresses(false);

            } catch (Exception e) {
                log.debug("Error retrieving local IP addresses:"
                    + e.getMessage());
            }
        }

        if (localIPs == null)
            return;

        stunResults.clear();

        // try all retrieved local addresses to bind for STUN request
        Collection<Thread> threads = new ArrayList<Thread>();
        for (InetAddress ip : localIPs) {
            // create and start new thread to perform the discovery
            Thread thread = new Thread(new StunDiscovery(ip, stunAddress,
                stunPort));
            threads.add(thread);
            thread.start();
        }

        if (blocking) {
            for (Thread thread : threads)
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // continue with next thread
                }
        }
    }

}
