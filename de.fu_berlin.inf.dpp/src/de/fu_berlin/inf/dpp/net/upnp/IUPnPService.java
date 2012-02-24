package de.fu_berlin.inf.dpp.net.upnp;

import java.util.List;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.IPreferenceStore;

public interface IUPnPService {

    public int getCurrentlyMappedPort();

    /**
     * Sets the {@link IPreferenceStore} for global settings, or null when
     * settings shall not stored in preferences. If an {@link IPreferenceStore}
     * is specified and port mapping is enabled in preferences, gateway
     * discovery is started.
     * 
     * @param upnpAccess
     * @param preferenceStore
     *            {@link IPreferenceStore} to store settings in, may be null to
     *            disable storage
     */
    public void init(IUPnPAccess upnpAccess, IPreferenceStore preferenceStore);

    /**
     * Returns the currently selected gateway to perform port mapping on.
     * 
     * @return currently selected {@link GatewayDevice}, or null if none.
     */
    public GatewayDevice getSelectedGateway();

    /**
     * Sets the {@link GatewayDevice} selected for Saros to perform port mapping
     * on. If the device changes and a mapping was made before, the mapping is
     * removed on the previous device and created on the new device.
     * 
     * @param gateway
     *            {@link GatewayDevice} selected for Saros to perform port
     *            mapping on.
     * @return true if the gateway device changed, false if gateway did not
     *         change or is null
     * 
     * @throws IllegalArgumentException
     *             if the given gateway is not a discovered one
     */
    public boolean setSelectedGateway(GatewayDevice gateway)
        throws IllegalArgumentException;

    /**
     * Returns all discovered gateways.
     * 
     * @return {@link List} of {@link GatewayDevice} found during UPnP
     *         discovery. Is <code>null</code> if discovery was not performed
     *         yet.
     */
    public List<GatewayDevice> getGateways();

    /**
     * Starts UPnp gateway discovery.
     * 
     * @param blocked
     *            Boolean flag whether the discovery should be performed
     *            blocking or concurrently
     */
    public void startGatewayDiscovery(boolean blocked);

    /**
     * Trigger the UPnP discovery.
     */
    public void discoverGateways();

    /**
     * A port mapping is created for the current Socks5Proxy port if a
     * Socks5Proxy is running.
     * 
     * @return true if port mapping was created, false otherwise
     */
    public boolean createSarosPortMapping();

    /**
     * Removes the port mapping for Saros.
     * 
     * @return true if port mapping removal was successful, false otherwise
     */
    public boolean removeSarosPortMapping();

    /**
     * Informs the user after checking certain conditions about a gateway
     * probably blocking him from connection requests. A warning bubble window
     * is displayed if: <li>this check was not performed before</li> <li>IBB
     * transport is not enforced in preferences</li><li>a gateway was discovered
     * by UPnP</li> <li>STUN discovery did not detected open access</li>
     */
    public void checkAndInformAboutUPnP();

    /**
     * Returns whether a port is currently mapped.
     * 
     * @return true if a port is currently mapped for Saros, false otherwise.
     */
    public boolean isMapped();

    public void setPreSelectedDeviceID(String preSelectedDeviceID);

    public String getPreSelectedDeviceID();

    /**
     * Retrieves and returns the public IP of the selected gateway. Is
     * <code>null</code> if no gateway is selected or IP retrieval failed.
     */
    public String getPublicGatewayIP();

}