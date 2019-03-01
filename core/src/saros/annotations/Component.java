package saros.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The single instance of this class per application is created by PicoContainer in the central
 * application context.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Component {

  /** Returns the name of the module this component belongs to. Default is "Misc" */
  String module() default DEFAULT_MODULE;

  public static final String DEFAULT_MODULE = "misc";
}
