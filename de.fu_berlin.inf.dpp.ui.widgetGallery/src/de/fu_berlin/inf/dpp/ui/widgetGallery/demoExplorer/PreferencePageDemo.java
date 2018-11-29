package de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class PreferencePageDemo extends AbstractDemo {
  protected IPreferencePage prefpage;

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(1, false));

    Composite content = new Composite(parent, SWT.NONE);
    content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    content.setLayout(new FillLayout());

    this.prefpage = this.getPreferencePage();
    this.prefpage.createControl(content);
  }

  public abstract IPreferencePage getPreferencePage();
}
