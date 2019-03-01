package saros.ui.widgetGallery.demoSuits.preferences;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  GeneralPageDemo.class,
  AdvancedPageDemo.class,
  CommunicationPageDemo.class,
  FeedbackPageDemo.class,
  NetworkPageDemo.class,
  PersonalizationPageDemo.class
})
@Demo
public class PreferencesDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite composite) {
    // nothing to do
  }
}
