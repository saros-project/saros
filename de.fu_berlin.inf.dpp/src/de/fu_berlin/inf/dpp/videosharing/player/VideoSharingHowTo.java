package de.fu_berlin.inf.dpp.videosharing.player;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ExplanationComposite;

public class VideoSharingHowTo extends ExplanationComposite {

    public class VideoSharingHowToComposite extends Composite {
        /* number of columns used for layout */
        final int numCols = 2;

        public VideoSharingHowToComposite(Composite parent, int style) {
            super(parent, style);

            this.setLayout(new GridLayout(numCols, false));

            Label explanation = new Label(this, SWT.WRAP);
            explanation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, numCols, 1));
            explanation
                .setText("In order to share your screen with a session participant please do the following steps:");

            Label step1 = new Label(this, SWT.NONE);
            step1.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
                false, true));
            step1.setText("1)");

            Label step1Text = new Label(this, SWT.WRAP);
            step1Text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
            step1Text.setText("Open Shared Project Session");

            Label step2 = new Label(this, SWT.NONE);
            step2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
                false, true));
            step2.setText("2)");

            Label step2Text = new Label(this, SWT.WRAP);
            step2Text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
            step2Text
                .setText("Select a participant with whom you want to share your screen");

            Label step3 = new Label(this, SWT.NONE);
            step3.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
                false, true));
            step3.setText("3)");

            Label step3Text = new Label(this, SWT.WRAP);
            step3Text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
            step3Text.setText("Click on the \""
                + VideoSharingAction.TOOLTIP_START_SESSION
                + "\" button in the view's toolbar");
        }
    }

    public VideoSharingHowTo(Composite parent, int style) {
        super(parent, style, SWT.ICON_INFORMATION);
        this.setLayout(new GridLayout(1, false));

        VideoSharingHowToComposite content = new VideoSharingHowToComposite(
            this, style);
        content.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
            true));

        this.setMinSize(new Point(160, 160));
    }

}
