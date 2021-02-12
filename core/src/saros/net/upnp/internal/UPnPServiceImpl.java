package saros.net.upnp.internal;

import java.io.IOException;
import saros.net.upnp.IUPnPService;
import saros.net.upnp.IGateway;
import saros.net.upnp.IGatewayFinder;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import saros.repackaged.picocontainer.Disposable;
import saros.util.NamedThreadFactory;


public final class UPnPServiceImpl implements IUPnPService, Disposable {

    private static final int LEASE_DURATION = 12 * 60 * 60;
    private final IGatewayFinder gatewayFinder;
    private final Map<IGateway, Map<String, Set<Integer>>> currentMappedPorts = new HashMap<>();
    private AtomicReference<List<IGateway>> discoveredGateways =
	new AtomicReference<List<IGateway>>();

    private final List<PortMappingRefreshTask> portMappingRefreshTasks = new ArrayList<>();

    private final ScheduledExecutorService portMappingRefreshScheduler =
	Executors.newSingleThreadScheduledExecutor(
		new NamedThreadFactory("upnp-portmapping-refresher"));

    public UPnPServiceImpl(final IGatewayFinder gatewayFinder) {
	this.gatewayFinder = gatewayFinder;
    }

    @Override
    public List<IGateway> getGateways(boolean forceRefresh) {
	List<IGateway> gateways = discoveredGateways.get();

	if (gateways != null && !forceRefresh) {
	    return new ArrayList<IGateway>(gateways);
	}
	try {
	    gateways = new ArrayList<IGateway>(gatewayFinder.discoverGateways());
	} catch (Exception e) {
	    return null;
	}
	discoveredGateways.set(gateways);
	return gateways;
    }

    @Override
    public synchronized boolean createPortMapping(final IGateway device, final int port, final String protocol, String description) {
	if (description == null) description = "";

	boolean isPersistentMapping = false;
    if (device.isMapped(port, protocol)) {
        if (!device.closePort(port, protocol)) return false;
    };  // TODO: Check if mapping belongs to another application
    int errCode = device.openPort(port, protocol, LEASE_DURATION, description);
    if (errCode == 725) {
        errCode = device.openPort(port, protocol, 0, description);
        isPersistentMapping = true;
    }
    if (errCode != 0) return false;


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

	if (!isPersistentMapping) {
	    final PortMappingRefreshTask task = new PortMappingRefreshTask(device, port, protocol, description);
	    final Future<?> future = portMappingRefreshScheduler.scheduleAtFixedRate(
		    task, LEASE_DURATION, LEASE_DURATION, TimeUnit.SECONDS);
	    task.setFuture(future);
	    portMappingRefreshTasks.add(task);
	}
	return true;
    }

    @Override
    public synchronized boolean deletePortMapping(final IGateway device, final int port, final String protocol) {
	if (!isMapped(device, port, protocol)) return false;
	boolean success = device.closePort(port, protocol);
	if (!success) return false;
	currentMappedPorts.get(device).get(protocol).remove(port);

	final PortMappingRefreshTask task =
	    portMappingRefreshTasks
	    .stream()
	    .filter(t -> t.device.equals(device) && t.protocol.equals(protocol) && t.port == port)
	    .findFirst()
	    .orElse(null);

	if (task != null) {
	    final Future<?> future = task.getFuture();
	    future.cancel(false);
	    if (!future.isCancelled()) {
		try {
		    future.get(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		}
		catch (ExecutionException e) {}
		catch (TimeoutException e) {}
	    }
	}
	// if (future.isDone()) {}
	portMappingRefreshTasks.remove(task);
	return true;
    }

    @Override
    public synchronized boolean isMapped(final IGateway device, int port, String protocol) {
	final Map<String, Set<Integer>> mappedPortsForDevice = currentMappedPorts.get(device);

	if (mappedPortsForDevice == null) return false;

	final Set<Integer> mappedPortsForProtocol = mappedPortsForDevice.get(protocol);

	if (mappedPortsForProtocol == null) return false;

	return mappedPortsForProtocol.contains(port);
    }

    @Override
    public InetAddress getExternalAddress(final IGateway device) {
	String externalIPaddress = device.getExternalIPAddress();
	final InetAddress address;
	try {
	    address = InetAddress.getByName(externalIPaddress);
	} catch (Exception e) {
	    return null;
	}
	if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress())
	    return null;

	return address;
    }

    @Override
    public synchronized void dispose() {
	for (Entry<IGateway, Map<String, Set<Integer>>> mappedPortsEntry :
		currentMappedPorts.entrySet()) {

	    final IGateway device = mappedPortsEntry.getKey();
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

    private void refreshPortMapping(final IGateway device, final int port, final String protocol, final String description) {
	try {
	    int errCode = device.openPort(port, protocol, LEASE_DURATION, description);
	    if (errCode != 0)
		throw new IOException("port mapping request replied with error code: " + errCode);
	} catch (Exception e) {
	}
    }

    private class PortMappingRefreshTask implements Runnable {
	private final IGateway device;
	private final int port;
	private final String protocol;
	private final String description;

	private Future<?> future;

	private PortMappingRefreshTask(
		final IGateway device,
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
}
