package de.fu_berlin.inf.dpp.ui.widgets.wizard.events;

/**
 * Listener for {@link EnterXMPPAccountCompositeListener} events.
 */
public interface EnterXMPPAccountCompositeListener {

    /**
     * Gets called whenever Saros's XMPP server has been chosen or another
     * server has been entered.
     * 
     * @param event
     */
    public void isSarosXMPPServerChanged(IsSarosXMPPServerChangedEvent event);

    /**
     * Gets called whenever the validity of the given XMPP server has changed.
     * 
     * @param event
     */
    public void xmppServerValidityChanged(XMPPServerChangedEvent event);

}