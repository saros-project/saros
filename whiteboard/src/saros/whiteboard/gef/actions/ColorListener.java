package saros.whiteboard.gef.actions;

import org.eclipse.swt.graphics.RGB;

/**
 * this interface will inform listeners when Colors for the user has been changed.
 *
 * @author Ben
 */
public interface ColorListener {

  /**
   * Usercolors has been changed. May be only 1 of those has been changed
   *
   * @param foreGround
   * @param backGround
   */
  public void updateColor(RGB foreGround, RGB backGround);
}
