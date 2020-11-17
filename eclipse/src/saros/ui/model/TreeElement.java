package saros.ui.model;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/** This class implements a default {@link ITreeElement} */
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
  public <T> T getAdapter(Class<T> adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }
}
