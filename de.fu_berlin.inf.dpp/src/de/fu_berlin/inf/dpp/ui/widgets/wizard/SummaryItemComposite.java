package de.fu_berlin.inf.dpp.ui.widgets.wizard;

import de.fu_berlin.inf.dpp.ui.widgets.SimpleIllustratedComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class SummaryItemComposite extends SimpleIllustratedComposite {
  protected IllustratedText defaultContent;

  public SummaryItemComposite(Composite parent, int style) {
    super(parent, style);

    this.defaultContent =
        new IllustratedText(
            parent.getDisplay().getSystemImage(SWT.ICON_WARNING),
            "No summary information available");
    this.setContent(null);
  }

  @Override
  public void setContent(IllustratedText illustratedText) {
    super.setContent(illustratedText != null ? illustratedText : this.defaultContent);
  }
}
