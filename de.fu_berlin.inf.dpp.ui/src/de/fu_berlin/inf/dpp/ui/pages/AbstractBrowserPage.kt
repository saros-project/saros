package de.fu_berlin.inf.dpp.ui.pages

import java.util.ArrayList
import de.fu_berlin.inf.dpp.ui.renderer.Renderer

/**
 * Abstract implementation of {@link IBrowserPage} which offers convenience
 * methods for registering browser functions and renderer.
 **
 * Creates a new BrowserPage that encapsulates the location and title of the
 * HTML page as well as the needed browsers functions and renderer.
 *
 * @param htmlDocName
 * the file name of the HTML document without any path addition
 * @param pageTitle
 * the title that will be shown in the dialog
 */

abstract class AbstractBrowserPage(htmlDocName: String?, pageTitle: String?) : IBrowserPage {
	/**
	 * Common HTML document location
	 */
	public override val relativePath: String = PATH + htmlDocName!!
	
	/**
	 * The title is shown to the user in the dialog.
	 */
	public override val title: String = pageTitle ?:  ""
	
	public override val renderers: MutableList<Renderer> = mutableListOf<Renderer>()

	public companion object{
		@JvmStatic
		val PATH = "html/dist/"
	}

}