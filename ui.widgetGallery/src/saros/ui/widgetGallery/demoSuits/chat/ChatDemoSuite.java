package saros.ui.widgetGallery.demoSuits.chat;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ChatControlDemo.class, ChatDemo.class /*
                                                    * , MultiUserChatDemo.class
                                                    */})
@Demo
public class ChatDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
