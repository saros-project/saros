package saros.net.upnp.internal;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import saros.net.upnp.IUPnPAccess;
import saros.net.upnp.IUPnPService;
import saros.repackaged.picocontainer.Disposable;
import saros.util.NamedThreadFactory;

/**
 * Class for performing UPnP functions (using the weupnp library) and managing the mapping state.
 */
public final class UPnPServiceImpl implements IUPnPService, Disposable {

  private static final Logger log = Logger.getLogger(UPnPServiceImpl.class);

  private final Map<GatewayDevice, Map<String, Set<Integer>>> currentMappedPorts = new HashMap<>();

  private final List<PortMappingRefreshTask> portMappingRefreshTasks = new ArrayList<>();

  /** Default lease duration for a port mapping. */
  private static final int LEASE_DURATION = 12 * 60 * 60;

  private final IUPnPAccess upnpAccess;

  private AtomicReference<List<GatewayDevice>> discoveredGateways =
      new AtomicReference<List<GatewayDevice>>();

  private final ScheduledExecutorService portMappingRefreshScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new NamedThreadFactory("upnp-portmapping-refresher"));

  public UPnPServiceImpl(final IUPnPAccess upnpAccess) {
    this.upnpAccess = upnpAccess;
  }

  @Override
  public synchronized boolean createPortMapping(
      final GatewayDevice device, final int port, final String protocol, String description) {

    if (description == null) description = "";

    boolean successfullyMapped = false;
    boolean isPersistentMapping = false;

    final String deviceName = getDeviceName(device);

    log.debug(
        deviceName
            + " - creating port mapping... - port="
            + port
            + ", protocol="
            + protocol
            + ", description="
            + description);

    try {

      final PortMappingEntry portMapping =
          upnpAccess.getSpecificPortMappingEntry(device, port, protocol);

      if (portMapping != null
          && !arePortMappingDescriptionsEqual(description, portMapping.getPortMappingDescription()))
        throw new IOException(
            "port is already mapped by another application: "
                + portMapping.getPortMappingDescription());

      /*
       * as we are working with leases we must remove them (primary to
       * ensure that the timer is canceled)
       */
      if (isMapped(device, port, protocol) && !deletePortMapping(device, port, protocol))
        throw new IOException("failed to remove existing port mapping");

      final InetAddress localAddress = device.getLocalAddress();

      int errorcode =
          upnpAccess.addPortMapping(
              device,
              port,
              port,
              localAddress.getHostAddress(),
              protocol,
              description,
              LEASE_DURATION);

      if (errorcode == 725)
        log.debug(deviceName + " - does not support lease duration for port mappings");

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
        throw new IOException("port mapping request replied with error code:" + errorcode);
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
      log.error(deviceName + " - failed to create port mapping: " + e.getMessage(), e);
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
      log.debug(deviceName + " - scheduling port mapping lease timer refresh");

      final PortMappingRefreshTask task =
          new PortMappingRefreshTask(device, port, protocol, description);

      final Future<?> future =
          portMappingRefreshScheduler.scheduleAtFixedRate(
              task, LEASE_DURATION, LEASE_DURATION, TimeUnit.SECONDS);

      task.setFuture(future);
      portMappingRefreshTasks.add(task);
    }

    log.debug(
        deviceName
            + " - sucessfully created port mapping - port="
            + port
            + ", protocol:"
            + protocol);
    return true;
  }

  @Override
  public synchronized boolean deletePortMapping(
      final GatewayDevice device, final int port, final String protocol) {

    final String deviceName = getDeviceName(device);

    if (!isMapped(device, port, protocol)) return false;

    log.debug(deviceName + " - deleting port mapping... - port=" + port + ", protocol=" + protocol);

    final PortMappingRefreshTask task =
        portMappingRefreshTasks.stream()
            .filter(t -> t.device.equals(device) && t.protocol.equals(protocol) && t.port == port)
            .findFirst()
            .orElse(null);

    if (task != null) {
      final Future<?> future = task.getFuture();

      log.debug(
          deviceName
              + " - canceling port mapping refresh task.. - port="
              + port
              + ", protocol="
              + protocol);

      future.cancel(false);

      if (!future.isCancelled()) {
        try {
          future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          log.warn(
              deviceName + " - interrupted while waiting for port mapping refresh task to finish");
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          log.error(
              deviceName + " - unexpected error waiting for port mapping refresh task finish", e);
        } catch (TimeoutException e) {
          log.warn(
              deviceName + " - timed out while waiting for port mapping refresh task to finsih");
        }
      }

      if (future.isDone())
        log.debug(
            deviceName
                + " - canceled port mapping refresh task - port="
                + port
                + ", protocol="
                + protocol);
    }

    portMappingRefreshTasks.remove(task);

    int errorcode = 0;

    try {
      errorcode = upnpAccess.deletePortMapping(device, port, protocol);

      if (errorcode != 0)
        throw new IOException("port mapping delete request replied with error code:" + errorcode);

    } catch (Exception e) {
      log.error(deviceName + " - failed to remove port mapping: " + e.getMessage(), e);
      // TODO re-add mapping refresh task
      return false;
    }

    currentMappedPorts.get(device).get(protocol).remove(port);

    log.debug(
        deviceName
            + " - sucessfully deleted port mapping - port="
            + port
            + ", protocol="
            + protocol);

    return true;
  }

  @Override
  public List<GatewayDevice> getGateways(boolean forceRefresh) {

    List<GatewayDevice> gateways = discoveredGateways.get();

    if (gateways != null && !forceRefresh) {
      log.debug("aborting gateway discovery due to cached results");
      return new ArrayList<GatewayDevice>(gateways);
    }

    log.debug("performing gateways discovery");

    try {
      // perform discovery
      gateways = new ArrayList<GatewayDevice>(upnpAccess.performDiscovery());
    } catch (Exception e) {
      log.error("performing gateway discovery failed: " + e.getMessage(), e);
      return null;
    }

    log.info("discovered " + gateways.size() + " gateway(s)");

    discoveredGateways.set(gateways);

    gateways = new ArrayList<GatewayDevice>(gateways);

    if (!gateways.isEmpty() && log.isTraceEnabled())
      log.trace("discovered gateways devices: " + gateways);

    return gateways;
  }

  @Override
  public synchronized boolean isMapped(GatewayDevice device, int port, String protocol) {
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
      log.warn("failed to discover external address of device: " + device.getFriendlyName(), e);
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
    log.debug("deleting all existing port mappings..");

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

    portMappingRefreshScheduler.shutdown();
  }

  private void refreshPortMapping(
      final GatewayDevice device, final int port, final String protocol, final String description) {
    final String deviceName = getDeviceName(device);

    log.debug(deviceName + " - refreshing port mapping - port=" + port + ", protocol=" + protocol);

    try {
      final InetAddress localAddress = device.getLocalAddress();

      int errorcode =
          upnpAccess.addPortMapping(
              device,
              port,
              port,
              localAddress.getHostAddress(),
              protocol,
              description,
              LEASE_DURATION);

      if (errorcode != 0)
        throw new IOException("port mapping request replied with error code:" + errorcode);

    } catch (Exception e) {
      log.error(deviceName + " - failed to refresh port mapping: " + e.getMessage(), e);
    }
  }

  private static boolean arePortMappingDescriptionsEqual(final String a, final String b) {
    if (a == null && b == null) return true;

    if (a == null || b == null) return true;

    if (a.equals(b)) return true;

    // for an unknown reason the FritzBox likes to replace spaces with points

    return a.replace(' ', '.').equals(b.replace(' ', '.'));
  }

  private static String getDeviceName(final GatewayDevice device) {
    String result = device.getFriendlyName();

    if (result == null || result.trim().isEmpty()) result = device.getModelName();

    if (result == null || result.trim().isEmpty()) result = device.getUSN();

    if (result == null || result.trim().isEmpty()) result = "Unknown Device";

    return result;
  }

  private class PortMappingRefreshTask implements Runnable {
    private final GatewayDevice device;
    private final int port;
    private final String protocol;
    private final String description;

    private Future<?> future;

    private PortMappingRefreshTask(
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
      refreshPortMapping(device, port, protocol, description);
    }

    private void setFuture(final Future<?> future) {
      this.future = future;
    }

    private Future<?> getFuture() {
      return future;
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
