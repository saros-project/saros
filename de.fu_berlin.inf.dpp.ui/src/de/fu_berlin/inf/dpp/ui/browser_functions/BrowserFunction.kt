package de.fu_berlin.inf.dpp.ui.browser_functions


/**
 * Use this annotation to annotate a method of {@link TypedJavascriptFunction}
 * subclasses. The {@linkplain TypedJavascriptFunction#function(Object[])
 * generic function(Object[]) call} will mapped the incoming JavaScript calls to
 * this method.
 * <p>
 * For consistency, the annotated method should have the same name as the
 * JavaScript function. This is not enforced programmatically.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class BrowserFunction(
	/**
	 * BrowserFunctions will be executed synchronously by default. Set to
	 * {@link Policy#ASYNC} to return immediately. Any return value of the
	 * method annotated with {@link BrowserFunction} will be disregarded then.
	 */
	val value: Policy = Policy.SYNC
) {
	/**
	 * Determines how the browser function will be executed: Either in the same
	 * thread as the caller ({@link #SYNC}) or in a new one.
	 */
	enum class Policy {
		SYNC, ASYNC
	}
}