package saros.ui.model;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Instances of this class are used in conjunction with {@link ITreeElement}s as generic {@link
 * TreeLabelProvider}s.
 *
 * @author bkahlert
 */
public class TreeLabelProvider extends StyledCellLabelProvider {
  @Override
  public void update(ViewerCell cell) {
    Object element = cell.getElement();
    if (element != null && element instanceof ITreeElement) {
      ITreeElement treeElement = (ITreeElement) element;

      StyledString styledString = treeElement.getStyledText();
      if (styledString != null) {
        cell.setText(styledString.toString());
        cell.setStyleRanges(styledString.getStyleRanges());
      } else {
        cell.setText(null);
      }

      cell.setImage(treeElement.getImage());
    }
  }
}
