package de.fu_berlin.inf.dpp.ui.model;

/**
 * This class implements a default {@link ITreeElement}
 * 
 * @author bkahlert
 */
public abstract class CheckBoxTreeElement extends TreeElement implements
    ICheckBoxTreeElement {

    public boolean isChecked() {
        return false;
    }

    public boolean isGrayed() {
        return false;
    }

}
