package de.fu_berlin.inf.dpp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Bind;

/**
 * This interface contains marker interfaces for binding components to specific
 * keys.
 * 
 * @see PicoContainer#getComponent(Object)
 * @see MutablePicoContainer#addComponent(Object, Object,
 *      org.picocontainer.Parameter...)
 * @see BindKey#bindKey(Class, Class)
 */
public interface ISarosContextBindings {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public @interface IBBTransport {
        // marker interface
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public @interface Socks5Transport {
        // marker interface
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public @interface SarosVersion {
        // marker interface
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public @interface PlatformVersion {
        // marker interface
    }
}
