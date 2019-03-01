package de.fu_berlin.inf.dpp.ui.widgets.decoration;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.picocontainer.annotations.Inject;

/**
 * Extends a {@link JID} displaying {@link Combo}.<br>
 * The {@link Combo} is pre-filled with the domains as defined in {@link XMPPAccountStore}.<br>
 * When the user edits the user portion of the selected {@link JID} all {@link Combo} elements are
 * updated in order to reflect the user portion accordingly.
 */
public class JIDCombo {

  @Inject Preferences preferences;

  @Inject XMPPAccountStore xmppAccountStore;

  protected Combo control;

  public JIDCombo(Combo control) {
    this.control = control;

    SarosPluginContext.initComponent(this);

    fill();
    update();
    registerListeners();

    SWTUtils.runSafeSWTAsync(
        null,
        new Runnable() {
          @Override
          public void run() {
            JIDCombo.this.control.setSelection(new Point(0, 0));
          }
        });
  }

  protected void fill() {
    String defaultServer = this.preferences.getDefaultServer();

    List<String> servers = this.xmppAccountStore.getDomains();
    if (servers.size() == 0) servers.add(defaultServer);
    this.control.removeAll();
    int selectIndex = 0;
    for (int i = 0, j = servers.size(); i < j; i++) {
      String server = servers.get(i);
      this.control.add("@" + server);
      if (defaultServer.equals(server)) selectIndex = i;
    }
    this.control.select(selectIndex);
  }

  protected void update() {
    /*
     * Save the current selection
     */
    Point selection = this.control.getSelection();

    String jid = this.control.getText();
    String username = (jid.contains("@")) ? jid.split("@")[0] : jid;

    String[] items = this.control.getItems();
    for (int i = 0; i < items.length; i++) {
      String item = items[i];
      item = username + "@" + item.split("@")[1];
      this.control.setItem(i, item);
    }

    /*
     * The modification of the list items changes the selection. Now, the
     * saved selection have to be set again.
     */
    this.control.setText(jid);
    this.control.setSelection(new Point(selection.x, selection.y));
  }

  protected void registerListeners() {
    this.control.addKeyListener(
        new KeyAdapter() {
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

  /** @see Combo#getText() */
  public String getText() {
    return this.control.getText();
  }

  /** @see Combo#setText(String) */
  public void setText(String string) {
    this.control.setText(string);
  }

  /** @see Combo#setEnabled(boolean) */
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
