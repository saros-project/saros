package saros.net.upnp;

import java.net.InetAddress;

public interface IGateway {

    /**
     * Returns USN (Unique Service Name) of gateway device
     *
     * @return USN of device
     */
    public String getUSN();

    /**
     * Returns the friendly (human readable) name associated with this device
     *
     * @return friendly name of device
     */
    public String getFriendlyName();

    /**
     * Retrieves the connection status ot the device
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    public boolean isConnected();

    /**
     * Returns the address used to reach this machine from the GatewayDevice
     *
     * @return local address
     */
    public InetAddress getLocalAddress();

    /**
     * Returns the address used to reach the GatewayDevice from this machine
     *
     * @return device address
     */
    public InetAddress getDeviceAddress();

    /**
     * Returns the external IP address associated with this device
     *
     * The external address is the address that can be used to connect to the
     * GatewayDevice from the external network
     * @return the external IP
     */
    public String getExternalIPAddress();

    /**
     * Returns whether a port is currently mapped on this device
     *
     * @param port port to check
     * @param protocol protocol to check
     * @return <code>true</code> if a port mapping is currently present, <code>false</code> otherwise
     */
    public boolean isMapped(int port, String protocol);

    /**
     * Deletes the port mapping associated with port and protocol
     *
     * @param port the port
     * @param protocol the protocol
     * @return <code>true</code> if deletion was successful, <code>false</code> otherwise
     */
    public boolean closePort(int port, String protocol);


    /**
     * Adds a new port mapping to the gateway using the supplied
     * parameters
     *
     * @param port associated with the mapping
     * @param protocol the protocol associated with the new mapping
     * @param description the mapping description
     * @param leaseDuration amount of seconds this mapping is valid. Use 0 for unlimited duration.
     * @return errorcode of the UPnP device reply, 0 if successful
     */
    public int openPort(int port, String protocol, int LEASE_DURATION, String description);
}
