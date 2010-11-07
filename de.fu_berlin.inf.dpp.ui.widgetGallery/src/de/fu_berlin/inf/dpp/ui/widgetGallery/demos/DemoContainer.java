package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

public class DemoContainer extends Demo {

	protected TabFolder tabFolder;

	public DemoContainer(Composite parent) {
		super(parent);
	}

	public DemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		this.tabFolder = new TabFolder(parent, SWT.NONE);
	}

	public TabFolder getTabFolder() {
		return this.tabFolder;
	}

	public void open(Demo demo) {
		this.tabFolder.setSelection(demo.getTabItem());
	}

}
