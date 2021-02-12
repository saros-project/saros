package saros.net.upnp;

import java.net.InetAddress;
import java.util.List;

public interface IUPnPService {

    public static final String TCP = "TCP";
    public static final String UDP = "UDP";


    /**
     * Returns all {@link IGateway gateway devices} in the current network environment
     * @param forceRefresh if <code>true</code> the current network environment will be rescanned
     * instead of using cached data
     * @return {@link List} of {@link IGateway} found during UPnP discovery or <code>null</code>
     * if the operation failed
     */
    public List<IGateway> getGateways(boolean forceRefresh);

    /**
     * Creates a port mapping (port forwarding) on the given gateway device. It is up to the
     * implementation to overwrite existing port mappings or refuse to overwrite them.
     *
     * @param device {@link IGateway} to create the port mapping for
     * @param port to map
     * @param protocol to use (TCP or UDP)
     * @param description for the port mapping or <code>null</code>
     * @return <code>true</code> if creating the port mapping was successful, <code>false</code>
     * otherwise
     */
    public boolean createPortMapping(IGateway gateway, int port, String protocol, String description);

    /**
     * Deletes a port mapping on the given gateway device.
     *
     * @param device {@link IGateway} to delete the port mapping from
     * @param port port of the mapping
     * @param protocol protocol of the mapping (TCP or UDP)
     * @return <code>true</code> if removing the port mapping was successful, <code>false</code>
     *     otherwise
     */
    public boolean deletePortMapping(IGateway gateway, int port, String protocol);

    /**
     * Returns whether a port is currently mapped on the given gateway device.
     *
     * @param device {@link IGateway} to check
     * @param port port to check
     * @param protocol protocol to check
     * @return <code>true</code> if a port mapping is currently present, <code>false</code> otherwise
     */
    public boolean isMapped(IGateway gateway, int port, String protocol);

    /**
     * Returns the external (IP) address of the given device. This is normally the address that was
     * assigned by an ISP to the current device during login (e.g PPPOE or other protocols).
     *
     * @param device {@link IGateway} the device to query
     * @return the external address or <code>null</code> if it does not exist
     */
    public InetAddress getExternalAddress(IGateway gateway);
}
