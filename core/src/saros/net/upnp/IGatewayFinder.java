package saros.net.upnp;

import java.util.List;

public interface IGatewayFinder {

    /**
     * Discovers gateway devices on the network the machine is connected to.
     *
     * If the host is connected to different networks via different interfaces
     * a gateway device corresponding to each network is retrieved. Assumes that
     * each network interface has a different InetAddress
     *
     * @return a List containig all found {@link IGateway gateway devices}
     */
    public List<IGateway> discoverGateways();
}
