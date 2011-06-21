package de.fu_berlin.inf.dpp.ui.util;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.net.UPnP.UPnPManager;

/**
 * Class for UPnP related UI methods
 */
public class UPnPUIUtils {

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
    public static void populateGaywaySelectionControls(
        final UPnPManager upnpManager, final Combo combo, final Label info,
        final Button checkbox) {

        // if the UPnPManager dont know about gateways, let him discover
        if (upnpManager.getGateways() == null)
            return;

        // if no devices are found, return now - nothing to populate
        if (upnpManager.getGateways().isEmpty()) {
            info.setText("No gateway found.");
            info.getParent().pack();
            return;
        }

        // insert found gateways into combobox
        int indexToSelect = 0;
        for (GatewayDevice gw : upnpManager.getGateways()) {
            try {
                String name = gw.getFriendlyName();
                if (!gw.isConnected())
                    name += " (disconnected)";

                combo.add(name);

                if (upnpManager.getSelectedGateway() != null
                    && gw.getUSN().equals(
                        upnpManager.getSelectedGateway().getUSN()))
                    indexToSelect = combo.getItemCount() - 1;
            } catch (Exception e) {
                // ignore faulty gateway
            }
        }

        // Configuration controls closed in the meanwhile?
        if (combo.isDisposed() || info.isDisposed() || checkbox.isDisposed())
            return;

        // if valid gateway found, show info and enable
        checkbox.setEnabled(true);
        combo.setVisible(true);
        combo.select(indexToSelect);
        combo.pack();
        info.setVisible(false);
        info.getParent().pack();
    }
}
