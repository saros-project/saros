package de.fu_berlin.inf.dpp.ui.widgets.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.JIDCombo;

/**
 * Gives the user the possibility to enter a {@link JID}, the corresponding
 * password and optionally a server with port and security settings.
 */
public class EnterXMPPAccountComposite extends Composite {

    private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

    private JIDCombo jidCombo;
    private Text passwordText;
    private ExpandableComposite serverOptionsExpandableComposite;
    private Text serverText;
    private Text portText;

    private Button useTSLButton;
    private Button useSASLButton;

    @Inject
    private PreferenceUtils preferenceUtils;

    public EnterXMPPAccountComposite(Composite composite, int style) {
        super(composite, style);
        super.setLayout(new GridLayout(2, false));

        SarosPluginContext.initComponent(this);

        /*
         * Row 1: JID
         */
        Label jidLabel = new Label(this, SWT.NONE);
        jidLabel.setText("XMPP/Jabber ID");

        Combo jidCombo = new Combo(this, SWT.BORDER);
        jidCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.jidCombo = new JIDCombo(jidCombo);

        /*
         * Row 2: Password
         */
        Label passwordLabel = new Label(this, SWT.NONE);
        passwordLabel.setText("Password");

        passwordText = new Text(this, SWT.BORDER);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        passwordText.setEchoChar('*');

        /*
         * Row 3: Server Options
         */
        new Label(this, SWT.NONE);

        serverOptionsExpandableComposite = new ExpandableComposite(this,
            SWT.NONE, ExpandableComposite.TWISTIE
                | ExpandableComposite.CLIENT_INDENT);
        serverOptionsExpandableComposite.setLayoutData(GridDataFactory
            .fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 50).create());
        serverOptionsExpandableComposite.setText("Advanced Options");
        serverOptionsExpandableComposite
            .addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    getParent().layout();
                }
            });

        Composite expandableCompositeContent = new Composite(
            serverOptionsExpandableComposite, SWT.NONE);
        expandableCompositeContent.setLayout(new GridLayout(2, false));
        serverOptionsExpandableComposite.setClient(expandableCompositeContent);

        new Label(expandableCompositeContent, SWT.NONE).setText("Server");

        serverText = new Text(expandableCompositeContent, SWT.BORDER);
        serverText
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(expandableCompositeContent, SWT.NONE).setText("Port");

        portText = new Text(expandableCompositeContent, SWT.BORDER);
        portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        useTSLButton = new Button(expandableCompositeContent, SWT.CHECK);
        useTSLButton.setText("Use TSL");

        useSASLButton = new Button(expandableCompositeContent, SWT.CHECK);
        useSASLButton.setText("Use SASL");

        hookListeners();
    }

    /**
     * @see Text#addModifyListener(ModifyListener)
     */
    public void addModifyListener(ModifyListener modifyListener) {
        this.modifyListeners.add(modifyListener);
    }

    /**
     * @see Text#removeModifyListener(ModifyListener)
     */
    public void removeModifyListener(ModifyListener modifyListener) {
        this.modifyListeners.remove(modifyListener);
    }

    private void notifyModifyText(ModifyEvent e) {
        for (ModifyListener modifyListener : this.modifyListeners) {
            modifyListener.modifyText(e);
        }
    }

    @Override
    public void setLayout(Layout layout) {
        // this composite handles the layout on its own
    }

    @Override
    public boolean setFocus() {
        return this.jidCombo.setFocus();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.jidCombo.setEnabled(enabled);
        this.passwordText.setEnabled(enabled);
        this.serverText.setEnabled(enabled);
    }

    private void hookListeners() {

        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                notifyModifyText(e);
            }
        };

        jidCombo.getControl().addModifyListener(modifyListener);
        passwordText.addModifyListener(modifyListener);
        serverText.addModifyListener(modifyListener);
        portText.addModifyListener(modifyListener);
    }

    /**
     * Returns the entered {@link JID}.
     * 
     * @return
     */
    public JID getJID() {
        return new JID(jidCombo.getText().trim());
    }

    /**
     * Sets the given {@JID} to the JID field.
     * 
     * @param jid
     */
    public void setJID(JID jid) {
        jidCombo.setText(jid.getBase());
    }

    /**
     * Returns the entered password.
     * 
     * @return
     */
    public String getPassword() {
        return passwordText.getText();
    }

    /**
     * Sets the given password to the password field.
     * 
     * @param password
     */
    public void setPassword(String password) {
        passwordText.setText(password);
    }

    /**
     * Returns the entered server in lower case letters.
     * 
     * @return empty string if no server has been provided
     */
    public String getServer() {
        return serverText.getText().trim().toLowerCase();
    }

    /**
     * Sets the given server to the server field.
     * 
     * @param server
     */
    public void setServer(String server) {
        serverText.setText(server);

        if (!(server.length() == 0)) {
            serverOptionsExpandableComposite.setExpanded(true);
        }
    }

    public boolean isUsingTSL() {
        return useTSLButton.getSelection();
    }

    public void setUsingTSL(boolean use) {
        useTSLButton.setSelection(use);
    }

    public boolean isUsingSASL() {
        return useSASLButton.getSelection();
    }

    public void setUsingSASL(boolean use) {
        useSASLButton.setSelection(use);
    }

    /**
     * Sets the given port to the port field.
     * 
     * @param port
     */
    public void setPort(String port) {
        portText.setText(port);
    }

    /**
     * Returns the entered port.
     * 
     * @return empty string if no port has been provided
     */
    public String getPort() {
        return portText.getText().trim();
    }

    /**
     * Returns true if the effectively used server is Saros's XMPP/Jabber server
     */
    public boolean isSarosXMPPServer() {
        String server = getServer();
        if (server.length() == 0)
            server = getJID().getDomain();
        return server.equalsIgnoreCase(preferenceUtils.getSarosXMPPServer());
    }
}
