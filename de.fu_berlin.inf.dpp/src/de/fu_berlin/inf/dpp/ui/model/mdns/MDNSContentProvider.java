package de.fu_berlin.inf.dpp.ui.model.mdns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

public final class MDNSContentProvider extends TreeContentProvider {

    private Viewer viewer;
    private JmDNS jmDNS;

    private boolean isDisposed;

    private final Map<String, ServiceInfo> mDNSServices = new HashMap<String, ServiceInfo>();

    private final ServiceListener serviceListener = new ServiceListener() {

        @Override
        public void serviceAdded(ServiceEvent event) {
            // NOP
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            final ServiceInfo info = event.getInfo();

            if (info == null)
                return;

            SWTUtils.runSafeSWTAsync(null, new Runnable() {

                @Override
                public void run() {
                    if (!isDisposed)
                        mDNSServices.remove(info.getQualifiedName());
                }
            });

            // runs async
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void serviceResolved(ServiceEvent event) {

            final ServiceInfo info = event.getInfo();

            if (info == null)
                return;

            SWTUtils.runSafeSWTAsync(null, new Runnable() {

                @Override
                public void run() {
                    if (!isDisposed)
                        mDNSServices.put(info.getQualifiedName(), info);
                }
            });

            // runs async
            ViewerUtils.refresh(viewer, true);
        }

    };

    public MDNSContentProvider() {
        // NOP
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        if (oldInput instanceof JmDNS)
            ((JmDNS) oldInput).removeServiceListener("_dpp._tcp.local.",
                serviceListener);

        jmDNS = null;

        if (newInput instanceof JmDNS) {
            jmDNS = (JmDNS) newInput;
            jmDNS.addServiceListener("_dpp._tcp.local.", serviceListener);
        }
    }

    @Override
    public void dispose() {
        isDisposed = true;

        if (jmDNS != null)
            jmDNS.removeServiceListener("_dpp._tcp.local.", serviceListener);

        jmDNS = null;
        mDNSServices.clear();
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(final Object inputElement) {

        if (!(inputElement instanceof JmDNS))
            return new Object[0];

        final List<Object> elements = new ArrayList<Object>();

        for (final ServiceInfo info : mDNSServices.values())
            elements.add(new MDNSEntryElement(info));

        return elements.toArray();
    }
}