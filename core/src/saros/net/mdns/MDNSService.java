package saros.net.mdns;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.apache.log4j.Logger;

/** MDNS Service using {@link JmDNS} library. */
// TODO use JmmDNS to support all network interfaces that are currently
// installed

// TODO reap dead DNS entries, JmDNSs reaper takes ages.
public class MDNSService {

  private static final Logger LOG = Logger.getLogger(MDNSService.class);

  private static final Charset CHARSET = Charset.forName("UTF-8");

  private boolean running;
  private boolean configured;

  private volatile JmDNS currentmDNS;

  private String currentServiceType;
  private String currentServiceName;

  private int currentServicePort;

  private Map<String, byte[]> currentServiceProperties = new HashMap<String, byte[]>();

  private volatile ServiceInfo currentServiceInfo;

  private final Map<String, ServiceInfo> resolvedDNSService =
      Collections.synchronizedMap(new HashMap<String, ServiceInfo>());

  private final List<ServiceListener> serviceListeners =
      new CopyOnWriteArrayList<ServiceListener>();

  private final ServiceListener forwarder =
      new ServiceListener() {

        @Override
        public void serviceAdded(ServiceEvent event) {

          LOG.trace("service added: " + event.getName());

          for (ServiceListener listener : serviceListeners) listener.serviceAdded(event);

          LOG.trace("resolving service: " + event.getName());
          event.getDNS().requestServiceInfo(event.getType(), event.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
          final ServiceInfo info = event.getInfo();

          if (info == null) return;

          LOG.trace("removed service: " + info.getQualifiedName());
          resolvedDNSService.remove(info.getQualifiedName());

          for (ServiceListener listener : serviceListeners) listener.serviceRemoved(event);
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
          final ServiceInfo info = event.getInfo();

          if (info == null) return;

          LOG.trace("resolved service: " + info.getQualifiedName());
          resolvedDNSService.put(info.getQualifiedName(), info);

          for (ServiceListener listener : serviceListeners) listener.serviceResolved(event);
        }
      };

  /**
   * Configures the service.
   *
   * @param type fully qualified service type name, such as <code>_http._tcp.local.</code>
   * @param name unqualified service instance name, such as <code>foobar</code>
   * @param port the local port on which the service runs
   * @param properties properties describing the service or <code>null</code>
   * @throws IllegalStateException if the service is already running.
   */
  public synchronized void configure(
      String type, String name, int port, Map<String, String> properties) {
    if (running)
      throw new IllegalStateException("service cannot be configured while it is running");

    currentServiceType = type;
    currentServiceName = name;
    currentServicePort = port;

    currentServiceProperties.clear();

    if (properties != null) {
      for (Entry<String, String> entry : properties.entrySet())
        currentServiceProperties.put(entry.getKey(), entry.getValue().getBytes(CHARSET));
    }

    configured = true;
  }

  /**
   * Starts the service.
   *
   * @throws IOException if the service could not be started
   */
  public synchronized void start() throws IOException {
    if (!configured) throw new IOException("service is not configured");

    if (running) return;

    resolvedDNSService.clear();

    currentmDNS = JmDNS.create();

    final ServiceInfo serviceInfo =
        ServiceInfo.create(
            currentServiceType,
            currentServiceName,
            currentServicePort,
            0,
            0,
            true,
            currentServiceProperties);

    currentmDNS.registerService(serviceInfo);

    currentServiceInfo = serviceInfo;
    running = true;

    LOG.info("DNS service started");
    LOG.debug("service announced as: " + serviceInfo.getQualifiedName());

    /*
     * service results during service registration will be include here so
     * it is safe to install the listener at this point
     */
    currentmDNS.addServiceListener(currentServiceType, forwarder);
  }

  /** Stops the service. */
  public synchronized void stop() {

    if (!running) return;

    try {
      currentmDNS.unregisterAllServices();
      currentmDNS.removeServiceListener(currentServiceType, forwarder);
      currentmDNS.close();
    } catch (IOException e) {
      LOG.error("failed to stop MDNS service", e);
    } finally {
      resolvedDNSService.clear();
      currentServiceInfo = null;
      currentmDNS = null;
      running = false;
    }

    LOG.info("DNS service stopped");
  }

  /**
   * Returns the fully qualified service name such as <code>foobar._http._tcp.local.</code> after
   * the service has been started.
   *
   * @return the fully qualified service name or <code>null</code> if the service is not running.
   */
  public String getQualifiedServiceName() {
    ServiceInfo serviceInfo = currentServiceInfo;

    if (serviceInfo == null) return null;

    return serviceInfo.getQualifiedName();
  }

  /**
   * Returns the currently resolved services.
   *
   * @return
   */
  public List<ServiceInfo> getResolvedServices() {
    synchronized (resolvedDNSService) {
      return new ArrayList<ServiceInfo>(resolvedDNSService.values());
    }
  }

  /**
   * Adds a service listener. The type of the service that is listened for is determined when the
   * service is configured.
   *
   * @param listener the listener to add
   * @see #configure(String, String, int, Map)
   */
  public void addServiceListener(ServiceListener listener) {
    serviceListeners.add(listener);
  }

  /**
   * Removes a service listener.
   *
   * @param listener the listener to remove
   */
  public void removeServiceListener(ServiceListener listener) {
    serviceListeners.remove(listener);
  }
}
