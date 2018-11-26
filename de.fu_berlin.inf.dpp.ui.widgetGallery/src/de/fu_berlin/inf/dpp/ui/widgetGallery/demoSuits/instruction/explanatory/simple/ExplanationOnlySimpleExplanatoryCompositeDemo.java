package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.simple;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanatoryComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

@Demo
public class ExplanationOnlySimpleExplanatoryCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    final SimpleExplanatoryComposite explanatoryComposite =
        new SimpleExplanatoryComposite(parent, SWT.NONE);

    Button contentControl = new Button(explanatoryComposite, SWT.NONE);
    explanatoryComposite.setContentControl(contentControl);
    contentControl.setText("Show the simple explanation...");
    contentControl.addSelectionListener(
        new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            String text =
                "I tell you how to use this composite.\n" + "This message closes in 5 seconds.";
            SimpleExplanation expl = new SimpleExplanation(null, text);
            explanatoryComposite.showExplanation(expl);

            Display.getCurrent()
                .timerExec(
                    5000,
                    new Runnable() {

                      @Override
                      public void run() {
                        explanatoryComposite.hideExplanation();
                      }
                    });
          }
        });
  }
}
