package de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DemoSuite {
  Class<? extends AbstractDemo>[] value();
}
