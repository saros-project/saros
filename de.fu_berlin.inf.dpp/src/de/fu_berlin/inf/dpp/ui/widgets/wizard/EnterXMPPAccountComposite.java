package de.fu_berlin.inf.dpp.ui.widgets.wizard;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.EmptyText;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.JIDCombo;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.EnterXMPPAccountCompositeListener;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.IsSarosXMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.XMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterXMPPAccountWizardPage;

/**
 * Gives the user the possibility to enter a {@link JID}, the corresponding
 * password and optionally a server.
 */
public class EnterXMPPAccountComposite extends Composite {
    private static final Logger log = Logger
        .getLogger(EnterXMPPAccountWizardPage.class);

    protected List<EnterXMPPAccountCompositeListener> enterXMPPAccountCompositeListeners = new ArrayList<EnterXMPPAccountCompositeListener>();
    protected List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected XMPPAccountStore xmppAccountStore;

    protected JIDCombo jidCombo;
    protected Text passwordText;
    protected ExpandableComposite serverOptionsExpandableComposite;
    protected EmptyText serverText;

    protected boolean lastIsSarosXMPPServer = false;
    protected String lastXMPPServer;

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

        this.passwordText = new Text(this, SWT.BORDER);
        this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.passwordText.setEchoChar('*');

        /*
         * Row 3: Server Options
         */
        new Label(this, SWT.NONE);

        serverOptionsExpandableComposite = new ExpandableComposite(this,
            SWT.NONE, ExpandableComposite.TWISTIE
                | ExpandableComposite.CLIENT_INDENT);
        serverOptionsExpandableComposite.setLayoutData(GridDataFactory
            .fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 50).create());
        serverOptionsExpandableComposite.setText("Server Options");
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

        Label serverLabel = new Label(expandableCompositeContent, SWT.NONE);
        serverLabel.setText("Server");

        Text serverText = new Text(expandableCompositeContent, SWT.BORDER);
        serverText
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.serverText = new EmptyText(serverText, "Optional");

        this.hookListeners();
    }

    /**
     * Adds a {@link EnterXMPPAccountCompositeListener}
     * 
     * @param enterXMPPAccountCompositeListener
     */
    public void addEnterXMPPAccountCompositeListener(
        EnterXMPPAccountCompositeListener enterXMPPAccountCompositeListener) {
        this.enterXMPPAccountCompositeListeners
            .add(enterXMPPAccountCompositeListener);
    }

    /**
     * @see Text#addModifyListener(ModifyListener)
     */
    public void addModifyListener(ModifyListener modifyListener) {
        this.modifyListeners.add(modifyListener);
    }

    /**
     * Removes a {@link EnterXMPPAccountCompositeListener}
     * 
     * @param enterXMPPAccountCompositeListener
     */
    public void removeEnterXMPPAccountCompositeListener(
        EnterXMPPAccountCompositeListener enterXMPPAccountCompositeListener) {
        this.enterXMPPAccountCompositeListeners
            .remove(enterXMPPAccountCompositeListener);
    }

    /**
     * @see Text#removeModifyListener(ModifyListener)
     */
    public void removeModifyListener(ModifyListener modifyListener) {
        this.modifyListeners.remove(modifyListener);
    }

    /**
     * Notify all {@link EnterXMPPAccountCompositeListener}s about a changed
     * xmpp server.
     */
    public void notifyIsSarosXMPPServerChanged() {
        boolean isSarosServer = this.isSarosXMPPServer();
        if (lastIsSarosXMPPServer != isSarosServer) {
            lastIsSarosXMPPServer = isSarosServer;

            IsSarosXMPPServerChangedEvent event = new IsSarosXMPPServerChangedEvent(
                isSarosServer);
            for (EnterXMPPAccountCompositeListener enterXMPPAccountCompositeListener : this.enterXMPPAccountCompositeListeners) {
                enterXMPPAccountCompositeListener
                    .isSarosXMPPServerChanged(event);
            }
        }
    }

    /**
     * Notify all {@ink EnterXMPPAccountCompositeListener}s if the validity of
     * the given XMPP server has changed.
     */
    protected void notifyXMPPServerValidityChanged() {
        String server = this.getServer();

        if (lastXMPPServer != server) {
            lastXMPPServer = server;
            XMPPServerChangedEvent event = new XMPPServerChangedEvent(server,
                isXMPPServerValid());
            for (EnterXMPPAccountCompositeListener enterXMPPAccountCompositeListener : this.enterXMPPAccountCompositeListeners) {
                enterXMPPAccountCompositeListener
                    .xmppServerValidityChanged(event);
            }
        }
    }

    /**
     * Notify all {@link EnterXMPPAccountCompositeListener}s about a changed
     * xmpp server.
     */
    public void notifyModifyText(ModifyEvent e) {
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

    protected void hookListeners() {
        /*
         * If user changes to another server than Saros's server, notify.
         */
        this.jidCombo.getControl().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!isSarosXMPPServer())
                    notifyIsSarosXMPPServerChanged();
            }
        });

        /*
         * We don't want trigger Saros server restriction warning until the user
         * really chose to use it (by filling out another input control).
         */
        this.jidCombo.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                notifyIsSarosXMPPServerChanged();
            }
        });

        /*
         * Check the server's validity on focus loss.
         */
        this.serverText.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                notifyXMPPServerValidityChanged();
            }
        });

        /*
         * Notify about text changes
         */
        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                notifyModifyText(e);
            }
        };
        this.jidCombo.getControl().addModifyListener(modifyListener);
        this.passwordText.addModifyListener(modifyListener);
        this.serverText.getControl().addModifyListener(modifyListener);
    }

    /**
     * Returns the entered {@link JID}.
     * 
     * @return
     */
    public JID getJID() {
        return new JID(this.jidCombo.getText().trim());
    }

    /**
     * Sets the given {@JID} to the jid field.
     * 
     * @param jid
     */
    public void setJID(JID jid) {
        this.jidCombo.setText(jid.getBase());
    }

    /**
     * Returns the entered password.
     * 
     * @return
     */
    public String getPassword() {
        return this.passwordText.getText();
    }

    /**
     * Sets the given password to the password field.
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.passwordText.setText(password);
    }

    /**
     * Returns the entered server.
     * 
     * @return empty string if no server has been provided
     */
    public String getServer() {
        return this.serverText.getText().trim();
    }

    /**
     * Sets the given server to the server field.
     * 
     * @param server
     */
    public void setServer(String server) {
        this.serverText.setText(server);

        /*
         * If server is not empty, open the server options
         */
        if (!server.isEmpty() && serverOptionsExpandableComposite != null
            && !serverOptionsExpandableComposite.isDisposed()) {
            serverOptionsExpandableComposite.setExpanded(true);
        }
    }

    /**
     * Returns true if the effectively used server is Saros's XMPP/Jabber server
     */
    public boolean isSarosXMPPServer() {
        String server = this.getServer();
        if (server.isEmpty())
            server = this.getJID().getDomain();
        return server.equalsIgnoreCase(preferenceUtils.getSarosXMPPServer());
    }

    /**
     * Return true if the entered server is valid.<br/>
     * A server is valid if it is empty or the name can be resolved.
     * 
     * @return
     */
    public boolean isXMPPServerValid() {
        boolean isXMPPServerValid;
        String server = getServer();
        if (server.isEmpty()) {
            isXMPPServerValid = true;
        } else {
            try {
                InetAddress address = InetAddress.getByName(getServer());
                isXMPPServerValid = true;
                log.debug("nslookup succeeded: " + getServer() + " = "
                    + address);
            } catch (UnknownHostException e) {
                isXMPPServerValid = false;
                log.debug("nslookup failed: " + getServer());
            }
        }
        return isXMPPServerValid;
    }
}
