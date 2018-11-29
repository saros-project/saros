package de.fu_berlin.inf.dpp.net.upnp.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.picocontainer.Disposable;

/*
 *  Class for performing UPnP functions (using the weupnp library) and managing the mapping state.
 */
@Component(module = "net")
public class UPnPServiceImpl implements IUPnPService, Disposable {

  private static final Logger LOG = Logger.getLogger(UPnPServiceImpl.class);

  private final Map<GatewayDevice, Map<String, Set<Integer>>> currentMappedPorts =
      new HashMap<GatewayDevice, Map<String, Set<Integer>>>();

  /*
   * srossbach: I have never encountered an UPNP gateway device that support
   * lease durations
   */
  /** Default lease duration is 12 hours. Is changed if router doesn't accept lease duration. */
  private static final int MAPPINGLEASEDURATION = 12 * 60 * 60;

  private final IUPnPAccess upnpAccess;

  private Timer mappingRefreshTimer;

  private AtomicReference<List<GatewayDevice>> discoveredGateways =
      new AtomicReference<List<GatewayDevice>>();

  private class MappingRefreshTask extends TimerTask {
    private final GatewayDevice device;
    private final int port;
    private final String protocol;
    private final String description;

    public MappingRefreshTask(
        final GatewayDevice device,
        final int port,
        final String protocol,
        final String description) {
      this.device = device;
      this.port = port;
      this.protocol = protocol;
      this.description = description;
    }

    @Override
    public void run() {
      createPortMapping(device, port, protocol, description);
    }
  }

  public UPnPServiceImpl(final IUPnPAccess upnpAccess) {
    this.upnpAccess = upnpAccess;
  }

  @Override
  public synchronized boolean createPortMapping(
      final GatewayDevice device, final int port, final String protocol, String description) {

    if (description == null) description = "";

    boolean successfullyMapped = false;
    boolean isPersistentMapping = false;

    try {

      PortMappingEntry portMapping = upnpAccess.getSpecificPortMappingEntry(device, port, protocol);

      if (portMapping != null && !description.equals(portMapping.getPortMappingDescription()))
        throw new IOException(
            "port is already mapped by another application: "
                + portMapping.getPortMappingDescription());

      /*
       * as we are working with leases we must remove them (primary to
       * ensure that the timer is canceled)
       */
      if (isMapped(device, port, protocol) && !deletePortMapping(device, port, protocol))
        throw new IOException("failed to removed previous port mapping");

      InetAddress localAddress = device.getLocalAddress();

      int errorcode =
          upnpAccess.addPortMapping(
              device,
              port,
              port,
              localAddress.getHostAddress(),
              protocol,
              description,
              MAPPINGLEASEDURATION);

      // in case mapping with lease duration fails, try without
      if (errorcode != 0) {
        isPersistentMapping = true;
        errorcode =
            upnpAccess.addPortMapping(
                device,
                port,
                port,
                localAddress.getHostAddress(),
                protocol,
                description,
                /* Persistent */ 0);
      }

      if (errorcode != 0) {
        throw new IOException(
            "device " + device.getFriendlyName() + " replied with error code=" + errorcode);
        // Not the right place to put user information to
        // if (errorcode == 403) {
        // SarosView
        // .showNotification(
        // "Setting up port mapping not allowed",
        // "The selected gateway supports UPnP discovery but apparently does not allow performing
        // port mapping.\n"
        // +
        // "You can probably enable port mapping in the gateway configuration. ");
        // }

      }

      successfullyMapped = true;

    } catch (Exception e) {
      LOG.error("creating port mapping failed: " + e.getMessage(), e);
      successfullyMapped = false;
    }

    if (!successfullyMapped) return false;

    Map<String, Set<Integer>> mappedPortsForDevice = currentMappedPorts.get(device);

    if (mappedPortsForDevice == null) {
      mappedPortsForDevice = new HashMap<String, Set<Integer>>();
      currentMappedPorts.put(device, mappedPortsForDevice);
    }

    Set<Integer> mappedPortsForProtocol = mappedPortsForDevice.get(protocol);

    if (mappedPortsForProtocol == null) {
      mappedPortsForProtocol = new HashSet<Integer>();
      mappedPortsForDevice.put(protocol, mappedPortsForProtocol);
    }

    mappedPortsForProtocol.add(port);

    /*
     * set timer to re-map after lease duration if mapping uses lease
     * duration
     */
    if (!isPersistentMapping) {
      assert mappingRefreshTimer == null;

      mappingRefreshTimer = new Timer();

      mappingRefreshTimer.schedule(
          new MappingRefreshTask(device, port, protocol, description), MAPPINGLEASEDURATION * 1000);
    }

    return true;
  }

  @Override
  public boolean deletePortMapping(
      final GatewayDevice device, final int port, final String protocol) {

    if (!isMapped(device, port, protocol)) return false;

    try {

      boolean success = upnpAccess.deletePortMapping(device, port, protocol) == 0;

      if (!success) return false;

      // Stop the remap timer
      if (mappingRefreshTimer != null) {
        mappingRefreshTimer.cancel();
        mappingRefreshTimer = null;
      }

      currentMappedPorts.get(device).get(protocol).remove(port);

      return true;

    } catch (Exception e) {
      LOG.error("removing port mapping failed: " + e.getMessage(), e);
    }
    return false;
  }

  // TODO this may could take a few seconds and may cause hanging if a GUI
  // thread accesses other methods in the meantime
  @Override
  public List<GatewayDevice> getGateways(boolean forceRefresh) {

    List<GatewayDevice> gateways = discoveredGateways.get();

    if (gateways != null && !forceRefresh) {
      LOG.debug("aborting gateway discovery due to cached results");
      return new ArrayList<GatewayDevice>(gateways);
    }

    LOG.debug("performing gateways discovery");

    try {
      // perform discovery
      gateways = new ArrayList<GatewayDevice>(upnpAccess.performDiscovery());
    } catch (Exception e) {
      LOG.error("performing gateway discovery failed: " + e.getMessage(), e);
      return null;
    }

    LOG.info("discovered " + gateways.size() + " gateway(s)");

    discoveredGateways.set(gateways);

    gateways = new ArrayList<GatewayDevice>(gateways);

    if (!gateways.isEmpty() && LOG.isTraceEnabled())
      LOG.trace("discovered gateways devices: " + gateways);

    return gateways;
  }

  @Override
  public boolean isMapped(GatewayDevice device, int port, String protocol) {
    final Map<String, Set<Integer>> mappedPortsForDevice = currentMappedPorts.get(device);

    if (mappedPortsForDevice == null) return false;

    final Set<Integer> mappedPortsForProtocol = mappedPortsForDevice.get(protocol);

    if (mappedPortsForProtocol == null) return false;

    return mappedPortsForProtocol.contains(port);
  }

  @Override
  public InetAddress getExternalAddress(GatewayDevice device) {
    final String externalIPaddress;

    try {
      externalIPaddress = device.getExternalIPAddress();
    } catch (Exception e) {
      LOG.warn("failed to discover external address of device: " + device.getFriendlyName(), e);
      return null;
    }

    final InetAddress address;

    try {
      address = InetAddress.getByName(externalIPaddress);
    } catch (UnknownHostException e) {
      // should not happen
      return null;
    }

    // some devices return 0.0.0.0 if they are not connected
    if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress())
      return null;

    return address;
  }

  @Override
  public synchronized void dispose() {
    LOG.debug("deleting existing port mappings");

    for (Entry<GatewayDevice, Map<String, Set<Integer>>> mappedPortsEntry :
        currentMappedPorts.entrySet()) {

      final GatewayDevice device = mappedPortsEntry.getKey();
      final Map<String, Set<Integer>> mappedPortsForProtocol = mappedPortsEntry.getValue();

      for (Entry<String, Set<Integer>> mappedPortsForProtocolEntry :
          mappedPortsForProtocol.entrySet()) {
        String protocol = mappedPortsForProtocolEntry.getKey();
        // create a copy or we will get a CME
        Set<Integer> mappedPorts = new HashSet<Integer>(mappedPortsForProtocolEntry.getValue());

        for (int port : mappedPorts) deletePortMapping(device, port, protocol);
      }
    }
  }

  // // FIXME remove this method it access the GUI
  // /**
  // * Informs the user after checking certain conditions about a gateway
  // * probably blocking him from connection requests. A warning bubble window
  // * is displayed if: <li>this check was not performed before</li> <li>IBB
  // * transport is not enforced in preferences</li><li>a gateway was
  // discovered
  // * by UPnP</li> <li>STUN discovery did not detected open access</li>
  // */
  // @Override
  // public void checkAndInformAboutUPnP() {
  // if (prefStore == null)
  // return;
  //
  // Utils.runSafeAsync("UPnPInfo", null, new Runnable() {
  //
  // @Override
  // public void run() {
  //
  // // return if IBB is forced
  // if (prefStore
  // .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT))
  // return;
  //
  // // test and return if I already warned
  // if (prefStore
  // .getBoolean(PreferenceConstants.GATEWAYCHECKPERFORMED))
  // return;
  //
  // // Remember that I checked for blocking gateway. To only
  // // do this once.
  // prefStore.setValue(PreferenceConstants.GATEWAYCHECKPERFORMED,
  // true);
  //
  // // perform UPnP discovery if not done before
  // if (gateways == null)
  // discoverGateways();
  //
  // // if there are no gateways detected, abort
  // if (gateways.isEmpty())
  // return;
  //
  // if (stunService.isDirectConnectionAvailable())
  // return;
  //
  // // Now we are ready to display the notification from SWT
  // // thread
  // SWTUtils.runSafeSWTAsync(null, new Runnable() {
  //
  // @Override
  // public void run() {
  //
  // // Show notification
  // SarosView
  // .showNotification(
  // "Possibly blocking gateway found",
  // "Saros had to use a slow fallback data connection mode.\n"
  // +
  // "A NAT gateway was detected in your LAN possibly blocking connections from non local peers.\n"
  // +
  // "For faster data exchange it may help if Saros setups a port mapping on your gateway. In the
  // preferences of Saros you can enable this feature (UPnP).");
  // }
  // });
  // }
  // });
  // }
}
