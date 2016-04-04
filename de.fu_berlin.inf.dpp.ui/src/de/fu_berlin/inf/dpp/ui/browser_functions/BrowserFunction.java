package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;

/**
 * Use this annotation to annotate a method of {@link TypedJavascriptFunction}
 * subclasses. The {@linkplain TypedJavascriptFunction#function(Object[])
 * generic function(Object[]) call} will mapped the incoming JavaScript calls to
 * this method.
 * <p>
 * For consistency, the annotated method should have the same name as the
 * JavaScript function. This is not enforced programmatically.
 * <p>
 * Note: Just because a method is annotated it's not automatically exposed (or
 * "injected") into the JavaScript context. For this, instances of the
 * surrounding class need to be created through the {@link HTMLUIContextFactory}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BrowserFunction {
    /**
     * BrowserFunctions will be executed synchronously by default. Set to
     * {@link Policy#ASYNC} to return immediately. Any return value of the
     * method annotated with {@link BrowserFunction} will be disregarded then.
     */
    Policy value() default Policy.SYNC;

    /**
     * Determines how the browser function will be executed: Either in the same
     * thread as the caller ({@link #SYNC}) or in a new one.
     */
    public enum Policy {
        SYNC, ASYNC
    }
}
