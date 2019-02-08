package de.fu_berlin.inf.dpp.ui.ide_embedding

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.IBrowser;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.pages.AbstractBrowserPage;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;

/**
 * IDE-independent base class for managing HTML dialogs.
 *
 * Those dialogs are displayed in a new window inside a browser. The
 * simultaneous display of multiple dialogs is supported. However, there may
 * only be one dialog open for each {@link IBrowserPage IBrowserPage} at the
 * same time.
 */

abstract class DialogManager(val uiSynchronizer : UISynchronizer? ) {
	
	companion object{
		val LOG = Logger.getLogger(DialogManager::class.java)
	}
	
	private var openDialogs = HashMap<String?, IBrowserDialog?>()
	
	/**
     * Shows a dialog displaying an HTML page inside a {@link IBrowser}. For
     * each {@link IBrowserPage} there may only be one open dialog window. If
     * this method is called when the dialog is already displayed, nothing
     * happens.
     * <p/>
     * May be called from any thread.
     *
     * @param browserPage
     *            the BrowserPage object to be displayed in the dialog
     */
	fun showDialogWindow(browserPage : IBrowserPage?){
		val page = browserPage
		if(page == null){
			return
		}
		uiSynchronizer?.asyncExec(object:Runnable{
			override fun run(){
				var ressource =page.relativePath
				if(dialogIsOpen(ressource)){
					
					// If the user try to open a dialog that is already open,
                    // the dialog should get active and in the foreground to
                    // help the user find it.
                    reopenDialogWindow(ressource);
                    return;
				}
				var dialog = createDialog(page)
				
				openDialogs.put(ressource, dialog)
			}
		})
	}
	
	/**
     * This method is to overwritten to implement the IDE-specific opening of
     * the dialog. This method must only be called on the UI thread.
     *
     * @param browserPage
     *            the BrowserPage object to be displayed in the dialog
     * @return an IDE-independent representation of the dialog
     */
	protected abstract fun createDialog(browserPage : IBrowserPage):IBrowserDialog
	
	/**
     * Closes the dialog displaying the given page.
     * <p/>
     * May be called from any thread.
     *
     * @param pageId
     *            a String representing the page that should be closed. Since we
     *            use the pageResource as an identifier, this string can be
     *            obtained via {@link IBrowserPage#getRelativePath()}
     */
	fun closeDialogWindow(pageId : String?){
		uiSynchronizer?.asyncExec(
			object:Runnable{
				override fun run(){
					val id = AbstractBrowserPage.PATH +pageId
						if(pageId == null || !dialogIsOpen(id)){
							LOG.warn(pageId + "could not be found")
							return
						}
					// shell is removed in the ShellLister
					openDialogs.get(id)!!.close()
				}
			})
	}
	
	/**
     * Set the location of a given dialog to the center of the eclipse instance.
     * If the given browserPage is not currently displayed in a shell/dialog
     * this does nothing.
     *
     * @param pageId
     *            a String representing the HTML page. Since we use the
     *            pageResource as an identifier, this string can be obtained via
     *            {@link IBrowserPage#getRelativePath()}
     */
    private fun reopenDialogWindow(relativPath : String?){
		if (relativPath == null || !dialogIsOpen(relativPath)) {
            LOG.warn(relativPath + "could not be found")
            return
        }
		
		var dialog = openDialogs.get(relativPath)
		dialog?.reopen()
	}
	
	/**
     * @param pageId
     *            a String representing the page. Since we use the pageResource
     *            as an identifier, this string can be obtained via
     *            {@link IBrowserPage#getRelativePath()}
     * @return true if the browserPage is currently displayed in a shell/dialog
     */
    private fun dialogIsOpen(relativPagePath : String?) : Boolean{
		return openDialogs.containsKey(relativPagePath)
	}
	
	/**
     * This method should be called in the IDE-specific close listeners to
     * remove the entry for the dialog.
     *
     * @param pageId
     *            a String representing the page. Since we use the pageResource
     *            as an identifier, this string can be obtained via
     *            {@link IBrowserPage#getRelativePath()}
     */
    protected fun removeDialogEntry(relativePath : String?){
		LOG.debug(relativePath + " is closed")
        openDialogs.remove(relativePath)
	}
}