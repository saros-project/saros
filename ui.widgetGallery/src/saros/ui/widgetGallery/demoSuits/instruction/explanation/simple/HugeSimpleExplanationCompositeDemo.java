package saros.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.SimpleExplanationComposite;
import saros.ui.widgets.SimpleExplanationComposite.SimpleExplanation;

@Demo("The following SimpleExplanationComposite has a minimal width/height of 300/300.")
public class HugeSimpleExplanationCompositeDemo extends AbstractDemo {
  public static int MIN_WIDTH = 300;
  public static int MIN_HEIGHT = 300;

  @Override
  public void createDemo(Composite parent) {
    SimpleExplanationComposite simpleExplanationComposite =
        new SimpleExplanationComposite(parent, SWT.NONE);
    simpleExplanationComposite.setMinSize(new Point(MIN_WIDTH, MIN_HEIGHT));
    SimpleExplanation simpleExplanation =
        new SimpleExplanation(
            SWT.ICON_INFORMATION,
            "This is a simple explanation with lots of text:\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur eget mi libero. Nunc at nibh turpis, vel dapibus ipsum. Integer vitae est eu purus malesuada tempus posuere nec sem. Aliquam congue ornare tempus. Vivamus elementum iaculis ipsum, et sodales turpis fringilla a. Duis viverra blandit lacus, egestas sagittis mauris dictum in. Phasellus sodales odio id urna congue eu faucibus elit auctor. Fusce lacinia commodo lacus ac suscipit. Etiam in tortor non ipsum interdum porttitor ornare ac elit. Aenean scelerisque erat at libero commodo at pretium nibh congue. Proin elit enim, laoreet eu iaculis nec, semper in mi. Ut rhoncus tempus mi sit amet dictum. Aliquam augue nisl, vestibulum in venenatis vitae, interdum at nulla. Aliquam non laoreet justo.\n\nSed lacinia facilisis lorem, vel mattis nibh suscipit sit amet. Curabitur ut justo purus, vel aliquam leo. Nulla sapien leo, volutpat euismod rutrum sit amet, venenatis sed felis. Ut vel purus purus, quis vulputate libero. Morbi rutrum auctor mi eu pellentesque. Sed sodales, leo in porttitor hendrerit, tortor quam blandit sapien, at blandit ante tellus quis erat. Aliquam odio nunc, iaculis vel suscipit dapibus, interdum a libero. Cras luctus enim in mi laoreet vehicula. Integer nec justo neque, et malesuada nunc. Vestibulum egestas suscipit interdum. Pellentesque accumsan lacus eu lacus congue sed vulputate risus mattis.\n\nVestibulum fermentum purus in mi tempor pretium. Pellentesque euismod commodo iaculis. Nulla euismod tempor felis, vel congue felis scelerisque eget. Suspendisse potenti. Cras viverra urna at sapien dapibus convallis. Ut tincidunt ipsum vel nulla pellentesque lacinia. Donec a lacus sem. Aenean volutpat dui sit amet mi auctor sed tempus purus faucibus. Morbi odio libero, ullamcorper vel hendrerit sit amet, sollicitudin consequat neque. Proin orci quam, euismod non sollicitudin id, tempus id nisi.\n\nAenean nec risus massa, id scelerisque eros. Duis porttitor, neque non suscipit iaculis, nulla arcu ornare orci, nec tincidunt dolor libero sit amet diam. Mauris faucibus faucibus felis a rutrum. Ut eu odio velit. Integer felis est, volutpat gravida ultricies eget, rutrum vitae magna. Sed et dignissim sapien. Mauris rutrum mauris elementum lorem varius faucibus. Morbi sed enim turpis. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec volutpat, ante non pharetra consequat, dolor diam placerat turpis, quis interdum orci nibh et urna. Suspendisse potenti. Sed ac libero leo, vel dictum metus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Proin nec feugiat nulla.");
    simpleExplanationComposite.setExplanation(simpleExplanation);
  }
}
