package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DemoContainer extends Demo {

	protected List<Demo> demos;
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
		this.tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem[] tabItems = tabFolder.getSelection();
				for (TabItem tabItem : tabItems) {
					for (Demo demo : demos) {
						if (demo.getTabItem() == tabItem) {
							demo.setVisible();
						}
					}
				}
			}
		});
	}

	public TabFolder register(Demo demo) {
		if (this.demos == null)
			this.demos = new ArrayList<Demo>();
		this.demos.add(demo);
		return this.tabFolder;
	}

	public void open(Demo demo) {
		this.tabFolder.setSelection(demo.getTabItem());
	}

}
