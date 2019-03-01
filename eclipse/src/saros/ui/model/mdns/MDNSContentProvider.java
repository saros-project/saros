package saros.ui.model.mdns;

import java.util.ArrayList;
import java.util.List;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.eclipse.jface.viewers.Viewer;
import saros.net.mdns.MDNSService;
import saros.ui.model.TreeContentProvider;
import saros.ui.util.ViewerUtils;

public final class MDNSContentProvider extends TreeContentProvider {

  private Viewer viewer;
  private MDNSService mDNSService;

  private final ServiceListener serviceListener =
      new ServiceListener() {

        @Override
        public void serviceAdded(ServiceEvent event) {
          // NOP
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
          ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
          /*
           * HACK find out why the viewer does not expand empty sub nodes. We
           * are currently facing the problem that it takes a while after
           * connecting before first contacts are shown even if we expand the
           * viewer in Connected state entries might still not be there so
           * that the user has to manually expand the tree.
           */

          ViewerUtils.expandAll(viewer);
          ViewerUtils.refresh(viewer, true);
        }
      };

  public MDNSContentProvider() {
    // NOP
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    if (oldInput instanceof MDNSService)
      ((MDNSService) oldInput).removeServiceListener(serviceListener);

    mDNSService = null;

    if (newInput instanceof MDNSService) {
      mDNSService = (MDNSService) newInput;
      mDNSService.addServiceListener(serviceListener);
    }

    this.viewer = viewer;
  }

  @Override
  public void dispose() {

    if (mDNSService != null) mDNSService.removeServiceListener(serviceListener);

    mDNSService = null;
  }

  @Override
  public Object[] getElements(final Object inputElement) {

    if (!(inputElement instanceof MDNSService)) return new Object[0];

    final MDNSService service = (MDNSService) inputElement;
    final List<Object> elements = new ArrayList<Object>();

    final String qualifiedServiceName = service.getQualifiedServiceName();

    for (final ServiceInfo info : service.getResolvedServices()) {
      if (info.getQualifiedName().equals(qualifiedServiceName)) continue; // do not display ourself

      elements.add(new MDNSEntryElement(info));
    }

    return elements.toArray();
  }
}
