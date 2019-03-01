package saros.ui.widgets.viewer;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * This abstract class is used for {@link Composite} that are based on a central {@link Viewer}. It
 * can be directly used as a {@link IPostSelectionProvider}.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link StructuredViewer}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @author bkahlert
 */
public abstract class ViewerComposite<T extends Viewer> extends Composite {
  public static final int VIEWER_STYLE =
      SWT.BORDER | SWT.NO_SCROLL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK | SWT.MULTI;

  private final T viewer;

  public ViewerComposite(Composite parent, int style) {
    super(parent, style & ~VIEWER_STYLE);

    viewer = createViewer(style & VIEWER_STYLE);
    configureViewer(viewer);
  }

  /**
   * Creates the viewer
   *
   * @param style
   */
  protected abstract T createViewer(int style);

  /**
   * Configures the viewer. <b>Important:</b> This method is called before the CTOR of the subclass
   * is processed. It is up to the caller to ensure that objects are already instantiated that are
   * used in this method !
   */
  protected abstract void configureViewer(T viewer);

  /** Return the internal {@link Viewer} */
  public T getViewer() {
    return viewer;
  }

  @Override
  public boolean setFocus() {
    if (viewer == null || viewer.getControl() == null) return false;

    return viewer.getControl().setFocus();
  }
}
