package de.fu_berlin.inf.dpp.ui.util;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.net.UPnP.UPnPManager;
import de.fu_berlin.inf.dpp.ui.Messages;

/**
 * Class for UPnP related UI methods
 */
public class UPnPUIUtils {
    protected static Logger log = Logger.getLogger(UPnPUIUtils.class);

    /**
     * Setups gateway SWT controls by populating a gateway combobox and
     * configuring an information Label and the enabling checkbox.
     * 
     * @param combo
     *            {@link Combo} selector to populate with discovered gateways
     * @param info
     *            {@link Label} displaying status information of the discovery
     * @param checkbox
     *            {@link Button} checkbox to enable/disable UPnP support
     */
    public static void populateGatewaySelectionControls(
        final UPnPManager upnpManager, final Combo combo, final Label info,
        final Button checkbox) {

        // Configuration controls closed in the meanwhile?
        if (combo.isDisposed() || info.isDisposed() || checkbox.isDisposed())
            return;

        combo.setEnabled(false);
        checkbox.setEnabled(false);
        combo.removeAll();

        // if no devices are found, return now - nothing to populate
        if (upnpManager.getGateways() == null
            || upnpManager.getGateways().isEmpty()) {
            info.setText(Messages.UPnPUIUtils_no_gateway);
            info.getParent().pack();
            return;
        }

        // insert found gateways into combobox
        int indexToSelect = 0;
        for (GatewayDevice gw : upnpManager.getGateways()) {
            try {
                String name = gw.getFriendlyName();
                if (!gw.isConnected())
                    name += Messages.UPnPUIUtils_disconnected;

                combo.add(name);

                if (upnpManager.getSelectedGateway() != null
                    && gw.getUSN().equals(
                        upnpManager.getSelectedGateway().getUSN()))
                    indexToSelect = combo.getItemCount() - 1;
            } catch (Exception e) {
                log.debug("Error updating UPnP selector:" + e.getMessage()); //$NON-NLS-1$
                // ignore faulty gateway
            }
        }

        // if valid gateway found, show info and enable
        if (combo.getItemCount() > 0) {
            checkbox.setEnabled(true);
            combo.setEnabled(true);
            combo.select(indexToSelect);
            combo.pack();
            info.setVisible(false);
        } else {
            info.setText(Messages.UPnPUIUtils_no_valid_gateway);
        }
        info.getParent().pack();
    }
}
