package de.fu_berlin.inf.dpp.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.actions.ChangeColorAction;

/**
 * This dialog will allow you to pick colors.
 * 
 * @author cnk and tobi
 */
public class ChangeColorDialog {

    private static final Logger log = Logger.getLogger(ChangeColorAction.class
        .getName());

    protected ISarosSession sarosSession;
    protected Shell parent;
    protected String title;

    public ChangeColorDialog(Shell parent, ISarosSession sarosSession, String title) {
        this.sarosSession = sarosSession;
        this.parent = parent;
        this.title = title;

    }
    
    public RGB open() {
        ColorDialog colorDialog = new ColorDialog(parent);
        colorDialog.setText(title);
        RGB selectedColor = colorDialog.open();
        return selectedColor;
    }
}