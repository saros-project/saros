package de.fu_berlin.inf.dpp.net.UPnP;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.StunHelper;
import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;
import de.javawi.jstun.test.DiscoveryInfo;

/*
 *  Class for performing UPnP functions (using the weupnp library) and managing the mapping state.
 */
@Component(module = "net")
public class UPnPManager {

    protected Logger log = Logger.getLogger(UPnPManager.class);

    @Inject
    StunHelper stunHelper;

    protected IUPnPAccess upnpAccess = null;

    private final String MAPPINGDESC = "Saros Socks5 TCP";

    /**
     * Default lease duration is 12 hours. Is changed if router doesn't accept
     * lease duration.
     */
    private int MAPPINGLEASEDURATION = 12 * 60 * 60;

    // Configuration
    protected IPreferenceStore prefStore = null;
    protected String preSelectedDeviceID = null;

    protected List<GatewayDevice> gateways = null;

    protected int currentlyMappedPort = 0;

    public int getCurrentlyMappedPort() {
        return currentlyMappedPort;
    }

    protected Timer mappingRefreshTimer = null;

    // The gateway selected to be configured.
    protected GatewayDevice selectedGateway = null;

    // binary semaphore to guard discovery from port mapping
    protected Semaphore discoveryRunningSemaphore = new Semaphore(1);

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
    public void init(IUPnPAccess upnpAccess, IPreferenceStore preferenceStore) {
        this.upnpAccess = upnpAccess;

        prefStore = preferenceStore;

        // when port mapping is enabled, discover gateways
        if (prefStore != null) {
            setPreSelectedDeviceID(prefStore
                .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID));

            if (!getPreSelectedDeviceID().isEmpty()) {
                startGatewayDiscovery(false);
            }
        }
    }

    /**
     * Returns the currently selected gateway to perform port mapping on.
     * 
     * @return currently selected {@link GatewayDevice}, or null if none.
     */
    public GatewayDevice getSelectedGateway() {
        return selectedGateway;
    }

    /**
     * Sets the {@link GatewayDevice} selected for Saros to perform port mapping
     * on. If the device changes and a mapping was made before, the mapping is
     * removed on the previous device and created on the new device.
     * 
     * @param gateway
     *            {@link GatewayDevice} selected for Saros to perform port
     *            mapping on.
     * @throws IllegalArgumentException
     *             if the given gateway is not a discovered one
     */
    public void setSelectedGateway(GatewayDevice gateway)
        throws IllegalArgumentException {

        if (selectedGateway == gateway)
            return;

        if (gateways == null)
            throw new IllegalArgumentException(
                "Setting a gateway when no gateways found is invalid.");

        if (gateway != null && gateways.contains(gateway) == false)
            throw new IllegalArgumentException("Unknown gateway device");

        if (isMapped() && selectedGateway != null)
            removeSarosPortMapping();

        selectedGateway = gateway;
        setPreSelectedDeviceID(gateway == null ? "" : gateway.getUSN());

        if (gateway != null)
            createSarosPortMapping();

        if (prefStore != null)
            prefStore.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID,
                getPreSelectedDeviceID());
    }

    /**
     * Returns all discovered gateways.
     * 
     * @return {@link List} of {@link GatewayDevice} found during UPnP
     *         discovery.
     */
    public List<GatewayDevice> getGateways() {
        return gateways;
    }

    /**
     * Starts UPnp gateway discovery.
     * 
     * @param blocked
     *            Boolean flag whether the discovery should be performed
     *            blocking or concurrently
     */
    public void startGatewayDiscovery(boolean blocked) {
        if (blocked)
            discoverGateways();
        else
            Utils.runSafeAsync("Saros UPnP discovery", log, new Runnable() {
                public void run() {
                    discoverGateways();
                }
            });
    }

    /**
     * Trigger the UPnP discovery.
     */
    public synchronized void discoverGateways() {

        try {
            discoveryRunningSemaphore.acquire();
        } catch (InterruptedException e1) {
            log.error(e1);
            return;
        }

        selectedGateway = null;
        gateways = null;

        try {
            // perform discovery
            gateways = new ArrayList<GatewayDevice>(
                upnpAccess.performDiscovery());
        } catch (Exception e) {
            log.debug("Error discovering a gateway:" + e.getMessage());
        }

        if (gateways == null || gateways.isEmpty()) {
            log.debug("No gateway device found.");
        } else {
            log.debug(gateways.size() + " gateway(s) discovered.");
            for (GatewayDevice gw : gateways) {
                if (gw.getUSN().equals(getPreSelectedDeviceID())) {
                    selectedGateway = gw;
                    log.debug("Using selected device: " + gw.getFriendlyName());
                }
            }
        }

        if (selectedGateway != null) {
            checkAndRemoveOldMapping();
        }

        discoveryRunningSemaphore.release();
    }

    /**
     * Checks for an old port mapping stored in the {@link IPreferenceStore} and
     * removes it from the gateway.
     */
    protected void checkAndRemoveOldMapping() {
        if (prefStore == null)
            return;

        int oldport = prefStore
            .getInt(PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT);
        if (oldport != 0)
            try {
                if (upnpAccess.deletePortMapping(selectedGateway, oldport,
                    "TCP") == 0)
                    prefStore.setValue(
                        PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT, 0);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
    }

    /**
     * Store last mapped port in preferences.<br>
     * In case we can't shutdown orderly we can delete that port mapping safely
     * on next start.
     * 
     * @param port
     *            Last successfully mapped port, or 0 if removed port mapping
     */
    protected void setCurrentlyMappedPort(int port) {
        if (port == 0)
            log.debug("Port mapping for port " + currentlyMappedPort
                + " removed");
        else
            log.debug("Port mapping for port " + port + " added for "
                + selectedGateway.getLocalAddress().getHostAddress() + " on "
                + selectedGateway.getFriendlyName());

        currentlyMappedPort = port;

        if (prefStore != null)
            prefStore.setValue(
                PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT,
                currentlyMappedPort);
    }

    /**
     * A port mapping is created for the current Socks5Proxy port if a
     * Socks5Proxy is running.
     * 
     * @return true if port mapping was created, false otherwise
     */
    public boolean createSarosPortMapping() {

        if (!NetworkingUtils.isSocks5ProxyRunning())
            return false;

        int port = Socks5Proxy.getSocks5Proxy().getPort();

        // When still discovering, delay mapping
        if (discoveryRunningSemaphore.availablePermits() == 0)
            log.debug("Waiting for UPnP discovery to complete...");

        try {
            discoveryRunningSemaphore.acquire();
        } catch (InterruptedException e1) {
            log.error(e1);
            return false;
        }

        if (selectedGateway == null) {
            discoveryRunningSemaphore.release();
            return false;
        }

        if (port != currentlyMappedPort && currentlyMappedPort != 0) {
            // remove my previous mapping when port changes
            removeSarosPortMapping();
        } else if (currentlyMappedPort == 0) {
            // first time mapping, lets check if mapped port is free
            try {
                PortMappingEntry portMapping = upnpAccess
                    .getSpecificPortMappingEntry(selectedGateway, port, "TCP");
                if (portMapping != null) {

                    // There is already a port mapping!

                    // If it has Saros description then remove it else abort
                    String desc = portMapping.getPortMappingDescription();
                    if ((desc != null && desc.equals(MAPPINGDESC)))
                        selectedGateway.deletePortMapping(port, "TCP");
                    else {
                        log.debug("There is already a port mapping for the port ("
                            + port + ") configured. Port mapping aborted.");
                        selectedGateway.deletePortMapping(port, "TCP");
                        discoveryRunningSemaphore.release();
                        return false;
                    }
                }
            } catch (Exception e) {
                log.debug("Error performing port mapping:" + e.getMessage());
            }
        }

        try {
            InetAddress localAddress = selectedGateway.getLocalAddress();

            PortMappingEntry portMapping = upnpAccess
                .getSpecificPortMappingEntry(selectedGateway, port, "TCP");

            if (portMapping == null) {

                int errorcode = upnpAccess.addPortMapping(selectedGateway,
                    port, port, localAddress.getHostAddress(), "TCP",
                    MAPPINGDESC, MAPPINGLEASEDURATION);

                // in case mapping with lease duration fails, try without
                if (errorcode != 0 && MAPPINGLEASEDURATION > 0) {
                    MAPPINGLEASEDURATION = 0;
                    errorcode = upnpAccess.addPortMapping(selectedGateway,
                        port, port, localAddress.getHostAddress(), "TCP",
                        MAPPINGDESC, MAPPINGLEASEDURATION);
                }

                if (errorcode == 0) {

                    setCurrentlyMappedPort(port);

                    // set timer to re-map after lease duration if mapping uses
                    // lease duration
                    if (MAPPINGLEASEDURATION > 0) {
                        if (mappingRefreshTimer != null)
                            mappingRefreshTimer.cancel();

                        mappingRefreshTimer = new Timer();

                        mappingRefreshTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                // Redo port mapping when lease duration expires
                                removeSarosPortMapping();
                                createSarosPortMapping();
                            }
                        }, MAPPINGLEASEDURATION * 1000);
                    }

                } else {
                    if (errorcode == 403) {
                        SarosView
                            .showNotification(
                                "Setting up port mapping not allowed",
                                "The selected gateway supports UPnP discovery but appearently does not allow performing port mapping.\n"
                                    + "You can probably enable port mapping in the gateway configuration. ");
                    }

                    log.debug("Error adding a port mapping to gateway "
                        + selectedGateway.getFriendlyName() + ", errorcode="
                        + errorcode + ". Disabling port mapping.");
                    setSelectedGateway(null);
                    discoveryRunningSemaphore.release();

                    return false;
                }
            } else {
                discoveryRunningSemaphore.release();
                return false;
            }

        } catch (Exception e) {
            log.debug("Error creating a port mapping: " + e.getMessage());
            discoveryRunningSemaphore.release();
            return false;
        }

        // Play it safe, get my public IP from gateway and add it to Socks5Proxy
        // addresses
        try {
            String externalIP = selectedGateway.getExternalIPAddress();
            if (externalIP != null
                && !externalIP.isEmpty()
                && !Socks5Proxy.getSocks5Proxy().getLocalAddresses()
                    .contains(externalIP))
                Socks5Proxy.getSocks5Proxy().addLocalAddress(externalIP);
        } catch (Exception e) {
            log.debug("Error retrieving external IP from selected gateway.", e);
        }
        discoveryRunningSemaphore.release();

        return true;
    }

    /**
     * Removes the port mapping for Saros.
     * 
     * @return true if port mapping removal was successful, false otherwise
     */
    public boolean removeSarosPortMapping() {
        if (selectedGateway == null || currentlyMappedPort == 0)
            return false;

        try {

            boolean success = upnpAccess.deletePortMapping(selectedGateway,
                currentlyMappedPort, "TCP") == 0;
            if (success)
                setCurrentlyMappedPort(0);

            // Stop the remap timer
            if (mappingRefreshTimer != null) {
                mappingRefreshTimer.cancel();
                mappingRefreshTimer = null;
            }
            return success;

        } catch (Exception e) {
            log.debug("Error deleting port mapping: " + e.getMessage());
        }
        return false;
    }

    /**
     * Informs the user after checking certain conditions about a gateway
     * probably blocking him from connection requests. A warning bubble window
     * is displayed if: <li>this check was not performed before</li> <li>IBB
     * transport is not enforced in preferences</li><li>a gateway was discovered
     * by UPnP</li> <li>STUN discovery did not detected open access</li>
     */
    public void checkAndInformAboutUPnP() {
        if (prefStore == null)
            return;

        Utils.runSafeAsync(null, new Runnable() {

            public void run() {

                // return if IBB is forced
                if (prefStore
                    .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT))
                    return;

                // test and return if I already warned
                if (prefStore
                    .getBoolean(PreferenceConstants.GATEWAYCHECKPERFORMED))
                    return;

                // Remember that I checked for blocking gateway. To only
                // do this once.
                prefStore.setValue(PreferenceConstants.GATEWAYCHECKPERFORMED,
                    true);

                // perform UPnP discovery if not done before
                if (gateways == null)
                    discoverGateways();

                // if there are no gateways detected, abort
                if (gateways.isEmpty())
                    return;

                // perform Stun test to check for NATed environment
                if (stunHelper.getStunResults().isEmpty())
                    stunHelper.startWANIPDetection(
                        prefStore.getString(PreferenceConstants.STUN),
                        prefStore.getInt(PreferenceConstants.STUN_PORT), true);

                boolean bOpenAccess = true;
                for (DiscoveryInfo di : stunHelper.getStunResults()) {
                    if (!di.isOpenAccess())
                        bOpenAccess = false;
                }
                if (bOpenAccess)
                    return;

                // Now we are ready to display the notification from SWT
                // thread
                Utils.runSafeSWTAsync(null, new Runnable() {

                    public void run() {

                        // Show notification
                        SarosView
                            .showNotification(
                                "Possibly blocking gateway found",
                                "Saros had to use a slow fallback data connection mode.\n"
                                    + "A NAT gateway was detected in your LAN possibly blocking connections from non local peers.\n"
                                    + "For faster data exchange it may help if Saros setups a port mapping on your gateway. In the preferences of Saros you can enable this feature (UPnP).");
                    }
                });
            }
        });
    }

    /**
     * Returns whether a port is currently mapped.
     * 
     * @return true if a port is currently mapped for Saros, false otherwise.
     */
    public boolean isMapped() {
        return currentlyMappedPort != 0;
    }

    public void setPreSelectedDeviceID(String preSelectedDeviceID) {
        this.preSelectedDeviceID = preSelectedDeviceID;
    }

    public String getPreSelectedDeviceID() {
        return preSelectedDeviceID;
    }

    public String getExternalIP() {
        if (getSelectedGateway() != null)
            try {
                return getSelectedGateway().getExternalIPAddress();
            } catch (Exception e) {
                return null;
            }
        return null;
    }
}
