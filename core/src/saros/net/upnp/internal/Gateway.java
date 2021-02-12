package saros.net.upnp.internal;

import saros.net.upnp.IGateway;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;


public class Gateway implements IGateway{

    private String serviceType = null, controlURL = null;
    private String usn;
    private String friendlyName;
    private InetAddress localAddress;
    private InetAddress deviceAddress;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((usn == null) ? 0 : usn.hashCode());
        result = prime * result
            + ((friendlyName == null) ? 0 : friendlyName.hashCode());
        result = prime * result
            + ((serviceType == null) ? 0 : serviceType.hashCode());
        result = prime * result
            + ((controlURL == null) ? 0 : controlURL.hashCode());
        result = prime * result + ((deviceAddress == null) ? 0 : deviceAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Gateway other = (Gateway) obj;
        if (usn == null) {
            if (other.usn != null)
                return false;
        } else if (!usn.equals(other.usn))
            return false;
        if (friendlyName == null) {
            if (other.friendlyName != null)
                return false;
        } else if (!friendlyName.equals(other.friendlyName))
            return false;
        if (serviceType == null) {
            if (other.serviceType != null)
                return false;
        } else if (!serviceType.equals(other.serviceType))
            return false;
        if (controlURL == null) {
            if (other.controlURL != null)
                return false;
        } else if (!controlURL.equals(other.controlURL))
            return false;
        if (deviceAddress == null) {
            if (other.deviceAddress != null)
                return false;
        } else if (!deviceAddress.equals(other.deviceAddress))
            return false;
        return true;
    }

    public Gateway(byte[] data) throws Exception {
        String location = null;
        StringTokenizer st = new StringTokenizer(new String(data), "\n");
        while (st.hasMoreTokens()) {
            String s = st.nextToken().trim();
            if (s.isEmpty() || s.startsWith("HTTP/1.") || s.startsWith("NOTIFY *")) {
                continue;
            }
            String name = s.substring(0, s.indexOf(':')), val = s.length() >= name.length() ? s.substring(name.length() + 1).trim() : null;
            if (name.equalsIgnoreCase("location")) {
                location = val;  // location = URL for UPnP description for root device
            }
            if (name.equalsIgnoreCase("usn")) {
                usn = val;
            }

        }
        if (location == null) {
            throw new Exception("Unsupported Gateway");
        }
        Document d;
        d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(location);
        NodeList friendlyName = d.getElementsByTagName("friendlyName");
        this.friendlyName = friendlyName.item(0).getTextContent();
        NodeList services = d.getElementsByTagName("service");
        for (int i = 0; i < services.getLength(); i++) {
            Node service = services.item(i);
            NodeList n = service.getChildNodes();
            String serviceType = null, controlURL = null;
            for (int j = 0; j < n.getLength(); j++) {
                Node x = n.item(j);
                if (x.getNodeName().trim().equalsIgnoreCase("serviceType")) {
                    serviceType = x.getFirstChild().getNodeValue();
                } else if (x.getNodeName().trim().equalsIgnoreCase("controlURL")) {
                    controlURL = x.getFirstChild().getNodeValue();
                }
            }
            if (serviceType == null || controlURL == null) {
                continue;
            }
            if (serviceType.trim().toLowerCase().contains(":wanipconnection:") || serviceType.trim().toLowerCase().contains(":wanpppconnection:")) {
                this.serviceType = serviceType.trim();
                this.controlURL = controlURL.trim();
            }
        }
        if (controlURL == null) {
            throw new Exception("Unsupported Gateway");
        }
        int slash = location.indexOf("/", 7); //finds first slash after http://
        if (slash == -1) {
            throw new Exception("Unsupported Gateway");
        }
        location = location.substring(0, slash);
        if (!controlURL.startsWith("/")) {
            controlURL = "/" + controlURL;
        }
        controlURL = location + controlURL;
    }

    private Map<String, String> command(String action, Map<String, String> params) throws Exception {
        Map<String, String> ret = new HashMap<String, String>();
        String soap = "<?xml version=\"1.0\"?>\r\n" + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
            + "<SOAP-ENV:Body>"
            + "<m:" + action + " xmlns:m=\"" + serviceType + "\">";
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                soap += "<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">";
            }
        }
        soap += "</m:" + action + "></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        byte[] req = soap.getBytes();
        HttpURLConnection conn = (HttpURLConnection) new URL(controlURL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("SOAPAction", "\"" + serviceType + "#" + action + "\"");
        conn.setRequestProperty("Connection", "Close");
        conn.setRequestProperty("Content-Length", "" + req.length);
        conn.getOutputStream().write(req);
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
        NodeIterator iter = ((DocumentTraversal) d).createNodeIterator(d.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        Node n;
        while ((n = iter.nextNode()) != null) {
            try {
                if (n.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    ret.put(n.getNodeName(), n.getTextContent());
                }
            } catch (Throwable t) {
            }
        }
        conn.disconnect();
        return ret;
    }

    @Override
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetAddress getDeviceAddress() {
        return deviceAddress;
    }

    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setDeviceAddress(InetAddress deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String getUSN() {
        return usn;
    }

    @Override
    public boolean isConnected() {
        try {
            Map<String, String> r = command("GetStatusInfo", null);
            String connectionStatus = r.get("NewConnectionStatus");
            return (connectionStatus != null && connectionStatus.equalsIgnoreCase("Connected"));
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public String getExternalIPAddress() {
        try {
            Map<String, String> r = command("GetExternalIPAddress", null);
            return r.get("NewExternalIPAddress");
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public int openPort(int port, String protocol, int leaseDuration, String description) {
        if (!protocol.equals("UDP") && !protocol.equals("TCP")) {
            throw new IllegalArgumentException("Invalid protocol");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol);
        params.put("NewInternalClient", localAddress.getHostAddress());
        params.put("NewExternalPort", "" + port);
        params.put("NewInternalPort", "" + port);
        params.put("NewEnabled", "1");
        params.put("NewPortMappingDescription", description);
        params.put("NewLeaseDuration", String.valueOf(leaseDuration));

        try {
            Map<String, String> r = command("AddPortMapping", params);
            return (r.get("errorCode") == null) ? 0 : Integer.parseInt(r.get("errorCode"));
        } catch (Exception ex) {
            return -1;
        }
    }

    @Override
    public boolean closePort(int port, String protocol) {
        if (!protocol.equals("UDP") && !protocol.equals("TCP")) {
            throw new IllegalArgumentException("Invalid protocol");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol);
        params.put("NewExternalPort", "" + port);
        try {
            command("DeletePortMapping", params);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean isMapped(int port, String protocol) {
        if (!protocol.equals("UDP") && !protocol.equals("TCP")) {
            throw new IllegalArgumentException("Invalid protocol");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", protocol);
        params.put("NewExternalPort", "" + port);
        try {
            Map<String, String> r = command("GetSpecificPortMappingEntry", params);
            if (r.get("errorCode") != null) {
                throw new Exception();
            }
            return r.get("NewInternalPort") != null;
        } catch (Exception ex) {
            return false;
        }

    }

}
