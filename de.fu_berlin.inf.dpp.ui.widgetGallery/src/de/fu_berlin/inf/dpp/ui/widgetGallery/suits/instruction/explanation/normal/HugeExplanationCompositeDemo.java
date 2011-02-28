package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ExplanationComposite;

public class HugeExplanationCompositeDemo extends Demo {
	public HugeExplanationCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE,
				SWT.ICON_INFORMATION);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.wrap = true;
		expl.setLayout(rowLayout);

		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
	}
}
