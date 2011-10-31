package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.nebula.explanation.ExplanationComposite;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

@Demo
public class ExplanationCompositeDemo extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
	ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE,
		SWT.ICON_INFORMATION);
	expl.setLayout(LayoutUtils.createGridLayout());
	Button explContent_hide = new Button(expl, SWT.PUSH);
	explContent_hide.setText("I'm a button explanation.");
    }
}
