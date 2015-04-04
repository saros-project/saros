package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

/**
 * This interface encapsulates functionality to show and close HTML-based
 * dialogs.
 * Those dialogs are displayed in a new window inside a browser.
 * The simultaneous display of multiple dialogs is supported.
 * However, there may only be one dialog open for each webpage at the same time.
 */
public interface IDialogManager {

    /**
     * Shows a dialog displaying an HTML page.
     * For each page there may only be one open dialog window.
     * If this method is called when the dialog is already displayed,
     * nothing happens.
     *
     * May be called from any thread.
     *
     * @param startPage the BrowserPage object to be displayed in the dialog
     */
    public void showDialogWindow(BrowserPage startPage);

    /**
     * Closes the dialog displaying the given page.
     *
     * May be called from any thread.
     *
     * @param webPage a String representing the page, this string can be obtained
     *             via {@link BrowserPage#getWebpage()}
     */
    public void closeDialogWindow(String webPage);
}
