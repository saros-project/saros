package saros.ui.model;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * This class implements a default {@link ITreeElement}
 *
 * @author bkahlert
 */
public abstract class TreeElement implements ITreeElement {

  @Override
  public StyledString getStyledText() {
    return null;
  }

  @Override
  public Image getImage() {
    return null;
  }

  @Override
  public ITreeElement getParent() {
    return null;
  }

  @Override
  public Object[] getChildren() {
    return new Object[0];
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }
}
