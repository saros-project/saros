package de.fu_berlin.inf.dpp.ui.decorators;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.annotations.Inject;

/*
 * This class MUST be thread safe !
 *
 * http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv
 * /guide/workbench_advext_decorators.htm
 */
// TODO rename this class, requires change in the plugin.xml too

/**
 * Decorates project files and their parent folders belonging to the active editors of the remote
 * users.
 *
 * @see ILightweightLabelDecorator
 */
@Component(module = "eclipse")
public class SharedProjectFileDecorator implements ILightweightLabelDecorator {

  private static final Logger LOG = Logger.getLogger(SharedProjectFileDecorator.class.getName());

  private static final String IMAGE_PATH = "icons/ovr16/dot.png"; // $NON-NLS-1$

  private final List<ILabelProviderListener> listeners =
      new CopyOnWriteArrayList<ILabelProviderListener>();

  /** Contains a set of resources to an active editor. Each for every user. */
  /*
   * if we can ensure a clean shutdown a.k.a sessionEnded is called no
   * activity is dispatched any longer this map does not need to be
   * synchronized
   */
  private final Map<User, Set<IResource>> activeEditorResources =
      Collections.synchronizedMap(new HashMap<User, Set<IResource>>());

  // add +1 for default color
  private final MemoryImageDescriptor[] imageDescriptors =
      new MemoryImageDescriptor[SarosAnnotation.SIZE + 1];

  /** Default image descriptor index pointing to a neutral color. */
  private final int defaultImageDescriptorIndex = imageDescriptors.length - 1;

  private final Map<IResource, ImageDescriptor> resourceToImageMapping =
      new ConcurrentHashMap<IResource, ImageDescriptor>(16, 0.75F, 1);

  @Inject private EditorManager editorManager;

  @Inject private ISarosSessionManager sessionManager;

  @Inject private EclipseReferencePointManager eclipseReferencePointManager;

  private static class MemoryImageDescriptor extends ImageDescriptor {

    private final ImageData data;

    public MemoryImageDescriptor(ImageData data) {
      this.data = data;
    }

    @Override
    public ImageData getImageData() {
      return data;
    }
  }

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarting(ISarosSession session) {
          session.addListener(sessionListener);
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          resourceToImageMapping.clear();
          updateDecoration(null);
          activeEditorResources.clear();
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userColorChanged(User user) {
          updateImageDescriptorMapping();
          updateDecoration(null);
        }

        @Override
        public void userLeft(User user) {
          Set<IResource> oldResources = activeEditorResources.remove(user);
          updateImageDescriptorMapping();
          updateDecoration(oldResources);
        }
      };

  private final ISharedEditorListener editorListener =
      new ISharedEditorListener() {

        @Override
        public void editorActivated(User user, SPath filePath) {
          Set<IResource> oldResources = null;

          if (user.isLocal()) return;

          if (LOG.isTraceEnabled())
            LOG.trace("remote user: " + user + " activated an editor -> " + filePath);

          oldResources = activeEditorResources.remove(user);

          if (filePath != null) {

            IReferencePoint referencePoint = filePath.getReferencePoint();

            IPath referencePointRelativePath = filePath.getProjectRelativePath();

            IResource resource =
                eclipseReferencePointManager.getResource(
                    referencePoint, ResourceAdapterFactory.convertBack(referencePointRelativePath));

            if (resource != null) activeEditorResources.put(user, getResources(resource));
            else LOG.warn("resource for editor " + filePath + " does not exist locally");
          }

          updateImageDescriptorMapping();
          updateDecoration(oldResources);
        }
      };

  public SharedProjectFileDecorator() {

    SarosPluginContext.initComponent(this);

    initializeImageDescriptors();

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    editorManager.addSharedEditorListener(editorListener);

    ISarosSession session = sessionManager.getSession();

    if (session != null) sessionLifecycleListener.sessionStarting(session);
  }

  @Override
  public void decorate(Object element, IDecoration decoration) {
    ImageDescriptor imageDescriptor = resourceToImageMapping.get(element);

    if (imageDescriptor == null) return;

    decoration.addOverlay(imageDescriptor, IDecoration.TOP_RIGHT);
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    editorManager.removeSharedEditorListener(editorListener);
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  /**
   * Updates decorations for all resources contained in {@link #activeEditorResources}.
   *
   * @param additionalResources additional resources to decorate
   */
  private void updateDecoration(Collection<IResource> additionalResources) {
    Set<IResource> resourcesToUpdate = new HashSet<IResource>();

    synchronized (activeEditorResources) {
      for (Set<IResource> resources : activeEditorResources.values())
        resourcesToUpdate.addAll(resources);
    }

    if (additionalResources != null) resourcesToUpdate.addAll(additionalResources);

    updateDecoratorsAsync(resourcesToUpdate.toArray(new IResource[0]));
  }

  private void updateDecoratorsAsync(final Object[] updateElements) {

    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            LabelProviderChangedEvent event =
                new LabelProviderChangedEvent(SharedProjectFileDecorator.this, updateElements);

            for (ILabelProviderListener listener : listeners) {
              listener.labelProviderChanged(event);
            }
          }
        });
  }

  /** Returns all parent resources for the given resource. */
  private Set<IResource> getResources(IResource resource) {

    Set<IResource> resources = new HashSet<IResource>();

    if (resource == null) {
      LOG.warn("resource should not be null");
      return resources;
    }

    resources.add(resource);
    IResource parent = resource.getParent();

    while (parent != null) {
      resources.add(parent);
      parent = parent.getParent();
    }

    return resources;
  }

  /**
   * Updates the {@link #resourceToImageMapping} based on the contents of {@link
   * #activeEditorResources}.
   */
  private void updateImageDescriptorMapping() {
    resourceToImageMapping.clear();

    synchronized (activeEditorResources) {
      for (Entry<User, Set<IResource>> entry : activeEditorResources.entrySet()) {

        User user = entry.getKey();
        Set<IResource> resources = entry.getValue();

        for (IResource resource : resources) {

          ImageDescriptor descriptor = resourceToImageMapping.get(resource);

          if (descriptor == null) descriptor = getImageDescriptor(user);
          else descriptor = getImageDescriptor(null);

          resourceToImageMapping.put(resource, descriptor);
        }
      }
    }
  }

  /** Returns an image descriptor for the given user or a default one if no user is provided. */
  private ImageDescriptor getImageDescriptor(User user) {

    if (user == null) return imageDescriptors[defaultImageDescriptorIndex];

    int colorID = user.getColorID();

    if (colorID < 0 || colorID >= SarosAnnotation.SIZE)
      return imageDescriptors[defaultImageDescriptorIndex];

    return imageDescriptors[colorID];
  }

  private void initializeImageDescriptors() {
    Image tintImage = ImageManager.getImage(IMAGE_PATH);

    for (int i = 0; i <= SarosAnnotation.SIZE; i++) {
      Color tintColor = SarosAnnotation.getUserColor(i);
      Image tintedImage = tintImage(tintImage, tintColor);
      imageDescriptors[i] = new MemoryImageDescriptor(tintedImage.getImageData());
      tintColor.dispose();
      tintedImage.dispose();
    }

    tintImage.dispose();
  }

  // TODO move to an utility class
  /** Returns null, if the image is not a RGB image with 8 Bit per color value */
  private static Image tintImage(Image image, Color color) {
    ImageData data = image.getImageData();
    int red = color.getRed();
    int green = color.getGreen();
    int blue = color.getBlue();

    if (data.depth < 24 || !data.palette.isDirect) return null;

    int rs = data.palette.redShift;
    int gs = data.palette.greenShift;
    int bs = data.palette.blueShift;
    int rm = data.palette.redMask;
    int gm = data.palette.greenMask;
    int bm = data.palette.blueMask;

    if (rs < 0) rs = ~rs + 1;

    if (gs < 0) gs = ~gs + 1;

    if (bs < 0) bs = ~bs + 1;

    for (int x = 0; x < data.width; x++) {
      for (int y = 0; y < data.height; y++) {
        int p = data.getPixel(x, y);
        int r = (p & rm) >>> rs;
        int g = (p & gm) >>> gs;
        int b = (p & bm) >>> bs;
        r = (r * red) / 255;
        g = (g * green) / 255;
        b = (b * blue) / 255;
        data.setPixel(x, y, (r << rs) | (g << gs) | (b << bs));
      }
    }

    return new Image(image.getDevice(), data);
  }
}
