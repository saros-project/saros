package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.basic.illustrated;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ SimpleIllustratedCompositeDemo.class,
    IllustratedCompositeDemo.class })
@Demo
public class IllustratedCompositeDemoSuite extends AbstractDemo {

    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
