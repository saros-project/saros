package saros.communication.connection;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import saros.Saros;

/**
 * Component for resolving Eclipse proxy settings.
 *
 * <p><b>This component will only resolve Socks5 proxy settings.</b>
 */
public class Socks5ProxyResolver implements IProxyResolver {

  // TODO maybe inject the bundle context ?
  private final Saros plugin;

  public Socks5ProxyResolver(final Saros plugin) {
    this.plugin = plugin;
  }

  @Override
  public ProxyInfo resolve(final String host) {

    /*
     * FIXME currently disabled until an option is available to either use
     * Eclipse settings or not
     */
    if (true) return null;

    URI hostURI;

    try {
      hostURI = new URI(host);
    } catch (URISyntaxException e) {
      return null;
    }

    final IProxyService proxyService = getProxyService();

    if (proxyService == null || !proxyService.isProxiesEnabled()) return null;

    for (IProxyData pd : proxyService.select(hostURI)) {
      if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
        return ProxyInfo.forSocks5Proxy(
            pd.getHost(), pd.getPort(), pd.getUserId(), pd.getPassword());
      }
    }

    return null;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private IProxyService getProxyService() {
    BundleContext bundleContext = plugin.getBundle().getBundleContext();
    ServiceReference serviceReference =
        bundleContext.getServiceReference(IProxyService.class.getName());
    return (IProxyService) bundleContext.getService(serviceReference);
  }
}
