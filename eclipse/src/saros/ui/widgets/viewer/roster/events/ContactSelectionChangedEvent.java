package saros.ui.widgets.viewer.roster.events;

import saros.net.xmpp.JID;

public class ContactSelectionChangedEvent {
  private JID jid;
  private boolean isSelected;

  /**
   * @param jid of the contact who's selection changed
   * @param isSelected new selection state
   */
  public ContactSelectionChangedEvent(JID jid, boolean isSelected) {
    this.jid = jid;
    this.isSelected = isSelected;
  }

  public JID getJID() {
    return jid;
  }

  public boolean isSelected() {
    return isSelected;
  }
}
