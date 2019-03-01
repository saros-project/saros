package de.fu_berlin.inf.dpp.whiteboard.gef.commands;

import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGRootRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.HierarchicalRecordSet;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

/**
 * We create a copy to store the current state.
 *
 * <p>The copy is stored as a list of ElementRecords, however, only the top-most records without
 * their child nodes.
 *
 * @author jurke
 */
public class CopyRecordCommand extends Command {

  private HierarchicalRecordSet recordsToCopy = new HierarchicalRecordSet();
  private ArrayList<LayoutElementRecord> copiedRecords = new ArrayList<LayoutElementRecord>();;

  public boolean addElement(LayoutElementRecord node) {
    if (!isCopyableNode(node)) return false;
    recordsToCopy.insertRecord(node);
    return true;
  }

  @Override
  public boolean canExecute() {
    if (recordsToCopy == null || recordsToCopy.isEmpty()) return false;
    Iterator<ElementRecord> it = recordsToCopy.getRootRecords().iterator();
    ElementRecord er;
    while (it.hasNext()) {
      er = it.next();
      if (!isCopyableNode(er)) return false;
      if (!er.getParent().isPartOfVisibleDocument()) return false;
    }
    return true;
  }

  @Override
  public void execute() {
    if (canExecute()) {
      ElementRecord copy;
      for (ElementRecord er : recordsToCopy.getRootRecords()) {
        copy = er.getCopy(true);
        /*
         * it is undesirable to add anything with the same primary
         * weight to the same parent, thus it is reset.
         */
        copy.setPrimaryWeight(null);
        copiedRecords.add((LayoutElementRecord) copy);
      }
      Clipboard.getDefault().setContents(copiedRecords);
    }
  }

  @Override
  public boolean canUndo() {
    return false;
  }

  public boolean isCopyableNode(NodeRecord node) {
    if (node instanceof SVGRootRecord) return false;
    return true;
  }

  @Override
  public void dispose() {
    super.dispose();
    recordsToCopy.clear();
    recordsToCopy = null;
    // cannot clear as they may remain in the Clipboard
    copiedRecords = null;
  }
}
