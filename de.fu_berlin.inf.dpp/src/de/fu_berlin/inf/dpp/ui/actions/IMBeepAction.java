package de.fu_berlin.inf.dpp.ui.actions;

import java.awt.Toolkit;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;

public class IMBeepAction extends Action {
    public IMBeepAction() {
        super("Toggle beep");
        this.updateIcon();
        this.updateTooltip();
        this.setEnabled(true);
    }

    @Override
    public void run() {
        PlatformUI.getPreferenceStore();

        this.setOn(!this.isOn());
        this.updateIcon();
    }

    protected boolean isOn() {
        return PlatformUI.getPreferenceStore().getBoolean(
            PreferenceConstants.BEEP_UPON_IM);
    }

    protected void setOn(boolean on) {
        PlatformUI.getPreferenceStore().setValue(
            PreferenceConstants.BEEP_UPON_IM, on);

        updateTooltip();
    }

    public void beep() {
        if (this.isOn())
            Toolkit.getDefaultToolkit().beep();
    }

    public void updateIcon() {
        if (this.isOn()) {
            this.setImageDescriptor(ImageManager
                .getImageDescriptor("/icons/elcl16/speakeron.png"));
        } else {
            this.setImageDescriptor(ImageManager
                .getImageDescriptor("/icons/elcl16/speakeroff.png"));
        }
    }

    protected void updateTooltip() {
        if (this.isOn())
            setToolTipText("Turn beep notificaton off");
        else
            setToolTipText("Turn beep notification on");
    }
}
