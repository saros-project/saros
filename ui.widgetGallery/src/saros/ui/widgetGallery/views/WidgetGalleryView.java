package saros.ui.widgetGallery.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import saros.ui.widgetGallery.ImageManager;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoElement;
import saros.ui.widgetGallery.demoExplorer.DemoExplorer;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.util.CompositeUtils;
import saros.ui.widgetGallery.widgets.BannerComposite;
import saros.ui.widgetGallery.widgets.DemoBannerComposite;
import saros.ui.widgets.SimpleIllustratedComposite.IllustratedText;

public class WidgetGalleryView extends ViewPart {
  public static final String ID = "saros.ui.widgetGallery.views.WidgetGalleryView";
  public static SelectionProviderIntermediate selectionProviderIntermediate =
      new SelectionProviderIntermediate();

  protected DemoExplorer demoExplorer;
  protected DemoBannerComposite demoBannerComposite;
  protected Composite demoComposite;
  protected Composite content;
  protected AbstractDemo currentDemo;

  @Override
  public void createPartControl(final Composite parent) {
    parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());

    this.getSite().setSelectionProvider(selectionProviderIntermediate);

    // createBanner(parent);

    SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.FLAT);
    sashForm.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
    sashForm.setLayout(new FillLayout());

    createDemoExplorer(sashForm);
    createDemoArea(sashForm);
    sashForm.setSashWidth(5);
    sashForm.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    sashForm.setWeights(new int[] {30, 70});

    openDemo();
  }

  protected void createBanner(final Composite parent) {
    BannerComposite bannerComposite = new BannerComposite(parent, SWT.NONE);
    bannerComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    bannerComposite.setContent(
        new IllustratedText(ImageManager.WIDGET_GALLERY_32, "Saros Widget Gallery"));
  }

  protected DemoExplorer createDemoExplorer(SashForm sashForm) {
    demoExplorer = new DemoExplorer(sashForm, SWT.NONE);
    demoExplorer
        .getViewer()
        .addSelectionChangedListener(
            new ISelectionChangedListener() {
              @Override
              public void selectionChanged(SelectionChangedEvent event) {
                openDemo();
              }
            });
    return demoExplorer;
  }

  protected void createDemoArea(SashForm sashForm) {
    demoComposite = new Composite(sashForm, SWT.NONE);
    demoComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());

    /*
     * Headline
     */
    Composite headline = createHeadline(demoComposite);
    headline.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /*
     * Content
     */
    this.content = new Composite(demoComposite, SWT.NONE);
    this.content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  }

  /**
   * Creates the controls for this demo
   *
   * @param composite
   * @return
   */
  public Composite createHeadline(Composite composite) {
    demoBannerComposite = new DemoBannerComposite(composite, SWT.NONE);
    demoBannerComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    return demoBannerComposite;
  }

  public void openDemo() {
    DemoElement demoElement = demoExplorer.getSelectedDemoElement();
    if (demoElement == null) return;

    Demo meta = demoElement.getDemo().getAnnotation(Demo.class);
    String title =
        (meta != null && !meta.value().isEmpty())
            ? meta.value()
            : "Demo: " + demoElement.getStyledText().toString();
    demoBannerComposite.setContent(new IllustratedText(ImageManager.DEMO, title));
    demoComposite.layout();

    if (currentDemo != null) currentDemo.dispose();
    CompositeUtils.emptyComposite(content);

    try {
      currentDemo = demoElement.getDemo().newInstance();
      currentDemo.createPartControls(content);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setFocus() {
    // do nothing
  }
}
