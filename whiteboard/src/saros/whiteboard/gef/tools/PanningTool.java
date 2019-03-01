package saros.whiteboard.gef.tools;

import org.eclipse.draw2d.Cursors;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.tools.PanningSelectionTool;
import org.eclipse.swt.graphics.Cursor;
import saros.whiteboard.Activator;

/**
 * An adaption of the PanningSelectionTool for panning only
 *
 * @author jurke
 */
public class PanningTool extends PanningSelectionTool {

  /** set state to panning always */
  @Override
  protected boolean handleButtonDown(int which) {
    setState(PAN);
    return super.handleButtonDown(which);
  }

  /** always returns a the hand as curser */
  @Override
  protected Cursor getDefaultCursor() {
    return Cursors.HAND;
  }

  public static class PanningToolEntry extends ToolEntry {

    /** Creates a new PanningToolEntry. */
    public PanningToolEntry() {
      this(null);
    }

    /**
     * Constructor for PanningToolEntry.
     *
     * @param label the label
     */
    public PanningToolEntry(String label) {
      this(label, null);
    }

    /**
     * Constructor for PanningToolEntry.
     *
     * @param label the label
     * @param shortDesc the description
     */
    public PanningToolEntry(String label, String shortDesc) {
      super(
          label,
          shortDesc,
          Activator.getImageDescriptor("icons/etool16/hand.png"),
          Activator.getImageDescriptor("icons/etool16/hand.png"),
          PanningTool.class);
      if (label == null || label.length() == 0) setLabel("Panning");
      setUserModificationPermission(PERMISSION_NO_MODIFICATION);
    }
  }
}
