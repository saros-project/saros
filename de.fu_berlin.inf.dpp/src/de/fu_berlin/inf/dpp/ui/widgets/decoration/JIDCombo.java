package de.fu_berlin.inf.dpp.ui.widgets.decoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Extend's an {@link JID} displaying {@link Combo}.<br/>
 * The {@link Combo} is pre-filled with the domains as defined in
 * {@link XMPPAccountStore}.<br/>
 * When the user edits the user portion of the selected {@link JID} all
 * {@link Combo} elements are updated in order to reflect the user portion
 * accordingly.
 */
public class JIDCombo {

    @Inject
    PreferenceUtils preferenceUtils;

    @Inject
    XMPPAccountStore xmppAccountStore;

    protected Combo control;

    public JIDCombo(Combo control) {
        this.control = control;

        SarosPluginContext.initComponent(this);

        fill();
        update();
        registerListeners();

        Utils.runSafeSWTAsync(null, new Runnable() {
            public void run() {
                JIDCombo.this.control.setSelection(new Point(0, 0));
            }
        });
    }

    protected void fill() {
        JIDComboUtils.fillJIDCombo(this.control, this.preferenceUtils,
            this.xmppAccountStore);
    }

    protected void update() {
        JIDComboUtils.updateJIDCombo(this.control);
    }

    protected void registerListeners() {
        this.control.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.HOME:
                case SWT.END:
                case SWT.ARROW_UP:
                case SWT.ARROW_RIGHT:
                case SWT.ARROW_DOWN:
                case SWT.ARROW_LEFT:
                    // no need to update
                    break;
                default:
                    update();
                }
            }
        });
    }

    /**
     * Returns the underlying {@link Combo} {@link Control}.
     * 
     * @return
     */
    public Combo getControl() {
        return this.control;
    }

    /**
     * @see Combo#getText()
     */
    public String getText() {
        return this.control.getText();
    }

    /**
     * @see Combo#setText(String)
     */
    public void setText(String string) {
        this.control.setText(string);
        update();
    }

    /**
     * @see Combo#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.control.setEnabled(enabled);
    }

    /**
     * @return
     * @see Combo#setFocus()
     */
    public boolean setFocus() {
        return this.control.setFocus();
    }
}
