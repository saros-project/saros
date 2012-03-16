package de.fu_berlin.inf.dpp.ui.actions;

import java.awt.Toolkit;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

public class IMBeepAction extends Action {
	public IMBeepAction() {
		super(Messages.IMBeepAction_title);
		this.updateIcon();
		this.updateUI();
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

		updateUI();
	}

	public void beep() {
		if (this.isOn())
			Toolkit.getDefaultToolkit().beep();
	}

	public void updateIcon() {
		if (this.isOn()) {
			this.setImageDescriptor(ImageManager
					.getImageDescriptor("/icons/elcl16/speakeron.png")); //$NON-NLS-1$
		} else {
			this.setImageDescriptor(ImageManager
					.getImageDescriptor("/icons/elcl16/speakeroff.png")); //$NON-NLS-1$
		}
	}

	protected void updateUI() {
		this.setChecked(this.isOn());
		if (this.isOn()) {
			setToolTipText(Messages.IMBeepAction_off_tooltip);
		} else {
			setToolTipText(Messages.IMBeepAction_on_tooltip);
		}
	}
}
