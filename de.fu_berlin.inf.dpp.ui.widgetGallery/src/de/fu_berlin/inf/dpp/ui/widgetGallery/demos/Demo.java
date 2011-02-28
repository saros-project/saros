package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.ImageManager;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.EmptyText;

public abstract class Demo {

	/**
	 * {@link Control}Êthis {@link Demo} is based on.
	 */
	protected Control control;

	/**
	 * Contains all controls and the content.
	 */
	protected Composite rootComposite;

	/**
	 * Contains the console for debug information send by the {@link Demo}
	 * instance.
	 */
	protected EmptyText console;
	protected static final SimpleDateFormat consoleDateFormat = new SimpleDateFormat(
			"HH:mm:ss");

	protected int consoleHeight = 150;

	protected TabItem tabItem;
	protected Composite content;

	public Demo(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		this.control = root;
		root.setLayout(new FillLayout());
		createPartControls(root);
	}

	public Demo(DemoContainer parent, String title) {
		TabFolder tabFolder = parent.register(this);
		this.control = tabFolder;

		this.tabItem = new TabItem(tabFolder, SWT.NULL);
		this.tabItem.setText(title);

		/*
		 * tabItem's content
		 */
		this.rootComposite = new Composite(tabFolder, SWT.NONE);
		this.rootComposite.setLayout(new GridLayout(2, false));
		this.tabItem.setControl(this.rootComposite);

		/*
		 * Content part
		 */
		this.content = new Composite(this.rootComposite, SWT.NONE);
		this.content
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		/*
		 * Control part
		 */
		Composite controls = createControls(this.rootComposite);
		controls.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false,
				true, 1, 2));

		/*
		 * Console part
		 */
		Text console = new Text(this.rootComposite, SWT.V_SCROLL | SWT.BORDER
				| SWT.MULTI);
		console.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.console = new EmptyText(console, "Debug Console");
		this.hideConsole();

		recreateDemo();
	}

	TabItem getTabItem() {
		return this.tabItem;
	}

	/**
	 * Recreates the demo. Especially useful if debug mode is enabled.
	 */
	public void recreateDemo() {
		Control[] children = content.getChildren();
		for (Control child : children) {
			if (!child.isDisposed()) {
				child.dispose();
			}
		}
		this.content.setLayout(new FillLayout());
		createPartControls(content);
		content.layout();
	}

	/**
	 * Shows the console
	 */
	public void showConsole() {
		((GridData) this.console.getControl().getLayoutData()).heightHint = consoleHeight;
		this.console.getControl().setVisible(true);
		this.rootComposite.layout();
	}

	/**
	 * Hides the console
	 */
	public void hideConsole() {
		((GridData) this.console.getControl().getLayoutData()).heightHint = 0;
		this.console.getControl().setVisible(false);
		this.rootComposite.layout();
	}

	/**
	 * Adds a message to the console and shows it if hidden.
	 * 
	 * @param message
	 */
	public void addConsoleMessage(String message) {
		String newLine = consoleDateFormat.format(new Date()) + " " + message
				+ "\n";
		this.console.setText(this.console.getText() + newLine);
		this.console.getControl().setSelection(this.console.getText().length());
		this.showConsole();
	}

	/**
	 * Creates the controls for this demo
	 * 
	 * @param composite
	 * @return
	 */
	public Composite createControls(Composite composite) {
		Composite controls = new Composite(composite, SWT.NONE);
		controls.setLayout(LayoutUtils.createGridLayout());

		Button recreateButton = new Button(controls, SWT.FLAT);
		recreateButton
				.setLayoutData(new GridData(SWT.END, SWT.END, true, true));
		recreateButton.setImage(ImageManager.RELOAD);
		recreateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				recreateDemo();
			}
		});
		return controls;
	}

	/**
	 * Creates the content for this demo
	 * 
	 * @param parent
	 */
	public abstract void createPartControls(Composite parent);

	/**
	 * Is called when this demo is set visible
	 */
	public void setVisible() {
		// do nothing
	}

}
