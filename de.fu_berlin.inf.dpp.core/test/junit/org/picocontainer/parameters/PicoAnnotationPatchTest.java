package org.picocontainer.parameters;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.annotations.Bind;
import org.picocontainer.injectors.AdaptingInjection;

public class PicoAnnotationPatchTest {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public static @interface TheString {
    // marker interface
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public static @interface NotTheString {
    // marker interface
  }

  private static final String THE_STRING = "THE STRING";
  private static final String NOT_THE_STRING = "NOT THE STRING";

  public static class TheClass {
    private final String theString;

    public TheClass(@TheString String theString) {
      this.theString = theString;
    }
  }

  @Test
  public void testCustomPicoAnnotationPatch() {
    MutablePicoContainer container = new PicoBuilder(new AdaptingInjection()).withCaching().build();

    container.addComponent(BindKey.bindKey(String.class, TheString.class), THE_STRING);

    container.addComponent(BindKey.bindKey(String.class, NotTheString.class), NOT_THE_STRING);

    container = container.makeChildContainer();

    container.addComponent(TheClass.class);

    assertEquals(THE_STRING, container.getComponent(TheClass.class).theString);
  }
}
