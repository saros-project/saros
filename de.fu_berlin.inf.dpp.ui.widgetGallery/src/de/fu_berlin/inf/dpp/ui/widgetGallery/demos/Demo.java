package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class Demo {

	protected TabItem tabItem;

	public Demo(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new FillLayout());
		createPartControls(root);
	}

	public Demo(DemoContainer parent, String title) {
		TabFolder tabFolder = parent.getTabFolder();

		this.tabItem = new TabItem(tabFolder, SWT.NULL);
		this.tabItem.setText(title);

		Composite root = new Composite(tabFolder, SWT.NONE);
		this.tabItem.setControl(root);

		root.setLayout(new FillLayout());
		createPartControls(root);
	}

	public TabItem getTabItem() {
		return this.tabItem;
	}

	public abstract void createPartControls(Composite parent);

}
