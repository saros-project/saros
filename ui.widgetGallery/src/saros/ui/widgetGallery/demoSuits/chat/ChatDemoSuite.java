package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.chat;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

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
