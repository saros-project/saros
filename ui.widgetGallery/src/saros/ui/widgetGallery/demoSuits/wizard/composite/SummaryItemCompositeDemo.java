package saros.ui.widgetGallery.demoSuits.wizard.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import saros.ui.ImageManager;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.util.StringUtils;
import saros.ui.widgets.SimpleIllustratedComposite.IllustratedText;
import saros.ui.widgets.wizard.SummaryItemComposite;

@Demo
public class SummaryItemCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(LayoutUtils.createGridLayout());

    Composite root = new Composite(parent, SWT.NONE);
    root.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
    root.setLayout(LayoutUtils.createGridLayout(0, 10));
    Image[] images =
        new Image[] {
          ImageManager.ICON_CONTACT,
          ImageManager.ICON_CONTACT_SAROS_SUPPORT,
          ImageManager.ICON_USER_SAROS_AWAY,
          ImageManager.ICON_USER_SAROS_FOLLOWMODE,
          ImageManager.ICON_USER_SAROS_FOLLOWMODE_AWAY,
          ImageManager.ICON_USER_SAROS_FOLLOWMODE_READONLY,
          ImageManager.ICON_USER_SAROS_FOLLOWMODE_READONLY_AWAY,
          ImageManager.ICON_USER_SAROS_READONLY,
          ImageManager.ICON_USER_SAROS_READONLY_AWAY,
          ImageManager.ICON_GROUP
        };

    for (Image image : images) {
      SummaryItemComposite summaryItemCompositeDemo = new SummaryItemComposite(root, SWT.NONE);
      summaryItemCompositeDemo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      summaryItemCompositeDemo.setContent(
          new IllustratedText(
              image, "Summary title\nSummary content: " + StringUtils.genRandom(32)));
    }
  }
}
