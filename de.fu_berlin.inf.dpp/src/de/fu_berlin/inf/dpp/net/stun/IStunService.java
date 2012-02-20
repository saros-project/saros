package de.fu_berlin.inf.dpp.net.stun;

import java.util.Collection;

import de.javawi.jstun.test.DiscoveryInfo;

public interface IStunService {

    /**
     * Returns whether a local retrieved IP is a public IP.
     * 
     * @return whether a local retrieved IP is a public IP.
     */
    public boolean isLocalIPthePublicIP();

    public Collection<DiscoveryInfo> getStunResults();

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
        boolean blocking);
}
