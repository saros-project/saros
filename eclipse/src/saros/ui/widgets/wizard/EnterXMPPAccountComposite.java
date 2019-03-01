package saros.ui.widgets.wizard;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
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
import saros.SarosPluginContext;
import saros.net.xmpp.JID;
import saros.preferences.Preferences;
import saros.ui.Messages;
import saros.ui.widgets.decoration.JIDCombo;

/**
 * Gives the user the possibility to enter a {@link JID}, the corresponding password and optionally
 * a server with port and security settings.
 */
public class EnterXMPPAccountComposite extends Composite {

  private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();
  private List<FocusListener> focusListeners = new ArrayList<FocusListener>();

  private JIDCombo jidCombo;
  private Text passwordText;
  private ExpandableComposite serverOptionsExpandableComposite;
  private Text serverText;
  private Text portText;

  private Button useTLSButton;
  private Button useSASLButton;

  @Inject private Preferences preferences;

  public EnterXMPPAccountComposite(Composite composite, int style) {
    super(composite, style);
    super.setLayout(new GridLayout(2, false));

    SarosPluginContext.initComponent(this);

    /*
     * Row 1: JID
     */
    Label jidLabel = new Label(this, SWT.NONE);
    jidLabel.setText(Messages.jid_shortform);

    Combo combo = new Combo(this, SWT.BORDER);
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    this.jidCombo = new JIDCombo(combo);

    /*
     * Row 2: Password
     */
    Label passwordLabel = new Label(this, SWT.NONE);
    passwordLabel.setText("Password");

    passwordText = new Text(this, SWT.BORDER);
    passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    passwordText.setEchoChar('*');

    /*
     * Row 3: Server Options
     */
    new Label(this, SWT.NONE);

    serverOptionsExpandableComposite =
        new ExpandableComposite(
            this, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);

    serverOptionsExpandableComposite.setText("Advanced Options");
    serverOptionsExpandableComposite.addExpansionListener(
        new ExpansionAdapter() {
          @Override
          public void expansionStateChanged(ExpansionEvent e) {
            getParent().layout();
          }
        });

    Composite expandableCompositeContent =
        new Composite(serverOptionsExpandableComposite, SWT.NONE);
    expandableCompositeContent.setLayout(new GridLayout(2, false));

    new Label(expandableCompositeContent, SWT.NONE).setText("Server");

    serverText = new Text(expandableCompositeContent, SWT.BORDER);
    serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    new Label(expandableCompositeContent, SWT.NONE).setText("Port");

    portText = new Text(expandableCompositeContent, SWT.BORDER);
    portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    useTLSButton = new Button(expandableCompositeContent, SWT.CHECK);
    useTLSButton.setText("Use TLS");

    useSASLButton = new Button(expandableCompositeContent, SWT.CHECK);
    useSASLButton.setText("Use SASL");

    serverOptionsExpandableComposite.setClient(expandableCompositeContent);

    /*
     * As the ExpandableComposite will tell us only the full height if it is
     * expanded but we want to show it not expanded we kindly "ask" the
     * composite about its size so that the parent of this composite can
     * correctly calculate its size and layout stuff.
     */
    serverOptionsExpandableComposite.setExpanded(true);

    Point defaultSize = serverOptionsExpandableComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    serverOptionsExpandableComposite.setExpanded(false);

    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

    data.widthHint = defaultSize.x;
    data.heightHint = defaultSize.y;

    serverOptionsExpandableComposite.setLayoutData(data);

    hookListeners();
  }

  public void addModifyListener(ModifyListener modifyListener) {
    modifyListeners.add(modifyListener);
  }

  public void removeModifyListener(ModifyListener modifyListener) {
    modifyListeners.remove(modifyListener);
  }

  @Override
  public void addFocusListener(FocusListener focusListener) {
    focusListeners.add(focusListener);
  }

  @Override
  public void removeFocusListener(FocusListener focusListener) {
    focusListeners.remove(focusListener);
  }

  @Override
  public void setLayout(Layout layout) {
    // this composite handles the layout on its own
  }

  @Override
  public boolean setFocus() {
    return jidCombo.setFocus();
  }

  @Override
  public void setEnabled(boolean enabled) {
    jidCombo.setEnabled(enabled);
    passwordText.setEnabled(enabled);
    serverText.setEnabled(enabled);
    portText.setEnabled(enabled);
    useTLSButton.setEnabled(enabled);
    useSASLButton.setEnabled(enabled);
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

  public boolean isUsingTLS() {
    return useTLSButton.getSelection();
  }

  public void setUsingTLS(boolean use) {
    useTLSButton.setSelection(use);
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

  /** Returns true if the effectively used server is Saros's XMPP server */
  public boolean isSarosXMPPServer() {
    String server = getServer();
    if (server.length() == 0) server = getJID().getDomain();
    return server.equalsIgnoreCase(preferences.getSarosXMPPServer());
  }

  private void hookListeners() {

    ModifyListener modifyListener =
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            notifyModifyText(e);
          }
        };

    FocusListener focusListener =
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            notifyFocusGained(e);
          }

          @Override
          public void focusLost(FocusEvent e) {
            notifyFocusLost(e);
          }
        };

    jidCombo.getControl().addModifyListener(modifyListener);
    passwordText.addModifyListener(modifyListener);
    serverText.addModifyListener(modifyListener);
    portText.addModifyListener(modifyListener);

    jidCombo.getControl().addFocusListener(focusListener);
    passwordText.addFocusListener(focusListener);
    serverText.addFocusListener(focusListener);
    portText.addFocusListener(focusListener);
  }

  private void notifyModifyText(ModifyEvent e) {
    for (ModifyListener modifyListener : modifyListeners) {
      modifyListener.modifyText(e);
    }
  }

  private void notifyFocusGained(FocusEvent e) {
    for (FocusListener focusListener : focusListeners) {
      focusListener.focusGained(e);
    }
  }

  private void notifyFocusLost(FocusEvent e) {
    for (FocusListener focusListener : focusListeners) {
      focusListener.focusLost(e);
    }
  }
}
