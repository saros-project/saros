package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.decoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.EmptyText;

@Demo
public class EmptyTextDemo extends AbstractDemo {

    protected Text createTextControl(final Composite parent, String emptyText) {

	Text textControl = new Text(parent, SWT.BORDER | SWT.MULTI);
	new EmptyText(textControl, emptyText);

	return textControl;
    }

    public void createDemo(Composite parent) {
	parent.setLayout(new GridLayout(2, false));

	String[] emptyTexts = new String[] { "Optional", "Please type here...",
		"Multline Text\n...\n...\n..." };

	for (int i = 0; i < emptyTexts.length; i++) {
	    Label label = new Label(parent, SWT.NONE);
	    label.setText("Default text: " + emptyTexts[i]
		    + ((i == emptyTexts.length - 1) ? "\nGridData.FILL" : ""));
	    label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
		    false));

	    Text textControl = createTextControl(parent, emptyTexts[i]);
	    textControl
		    .setLayoutData((i == emptyTexts.length - 1) ? new GridData(
			    SWT.FILL, SWT.FILL, true, true) : new GridData(
			    SWT.FILL, SWT.CENTER, true, false));
	}
    }
}
