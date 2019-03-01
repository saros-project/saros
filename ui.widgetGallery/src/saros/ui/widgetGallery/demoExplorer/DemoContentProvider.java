package saros.ui.widgetGallery.demoExplorer;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import saros.ui.model.TreeContentProvider;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

/**
 * {@link IContentProvider} for use in conjunction with a {@link AbstractDemo} input.
 *
 * <p>Automatically keeps track of changes of buddies.
 *
 * @author bkahlert
 */
public class DemoContentProvider extends TreeContentProvider {
  protected Viewer viewer;

  protected Class<? extends AbstractDemo> demo;

  public DemoContentProvider() {
    super();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.viewer = viewer;

    if (newInput instanceof Class<?>) {
      this.demo = (Class<? extends AbstractDemo>) newInput;
    } else {
      this.demo = null;
    }
  }

  @Override
  public void dispose() {
    // Nothing to do
  }

  /**
   * Returns {@link DemoSuite}s followed by {@link AbstractDemo}s which don't belong to any {@link
   * DemoSuite}.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object[] getElements(Object inputElement) {
    try {
      Class<? extends AbstractDemo> demo = (Class<? extends AbstractDemo>) inputElement;
      List<Object> elements = new ArrayList<Object>();

      DemoSuite demoSuite = demo.getAnnotation(DemoSuite.class);
      if (demoSuite != null) {
        for (Class<? extends AbstractDemo> subDemo : demoSuite.value())
          elements.add(new DemoElement(subDemo));
      }

      return elements.toArray();
    } catch (Exception e) {
      return new Object[0];
    }
  }

  @Override
  public boolean hasChildren(Object element) {
    return getChildren(element).length != 0;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof DemoElement) {
      return this.getElements(((DemoElement) parentElement).getDemo());
    }
    return new Object[0];
  }
}
