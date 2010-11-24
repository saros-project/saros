package de.fu_berlin.inf.dpp.ui.widgets.explanation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * This composite displays a simple {@link ExplanationComposite} and allows it's
 * content to be scrollable if the composite becomes to small.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout and adding
 * sub {@link Control}s correctly.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link ExplanationComposite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @see ExplanationComposite
 * @author bkahlert
 * 
 */
public class SimpleExplanationComposite extends ExplanationComposite {

    /**
     * Instances of this class are used to set the contents of an
     * {@link SimpleExplanationComposite} instance.
     * 
     * @see SimpleExplanationComposite#setExplanation(SimpleExplanation)
     */
    public static class SimpleExplanation {
        protected String explanationText;
        protected Image explanationImage;

        /**
         * Constructs a new explanation for use with
         * {@link SimpleExplanationComposite}.
         * 
         * @param explanationText
         *            The explanation to be shown next to the image
         */
        public SimpleExplanation(String explanationText) {
            this(null, explanationText);
        }

        /**
         * Constructs a new explanation for use with
         * {@link SimpleExplanationComposite}.
         * 
         * @param systemImage
         *            SWT constant that declares a system image (e.g.
         *            {@link SWT#ICON_INFORMATION})
         */
        public SimpleExplanation(int systemImage) {
            this(Display.getDefault().getSystemImage(systemImage), null);
        }

        /**
         * Constructs a new explanation for use with
         * {@link SimpleExplanationComposite}.
         * 
         * @param explanationImage
         *            Explanatory image {@link SWT#ICON_INFORMATION})
         */
        public SimpleExplanation(Image explanationImage) {
            this(explanationImage, null);
        }

        /**
         * Constructs a new explanation for use with
         * {@link SimpleExplanationComposite}.
         * 
         * @param systemImage
         *            SWT constant that declares a system image (e.g.
         *            {@link SWT#ICON_INFORMATION})
         * @param explanationText
         *            The explanation to be shown next to the image
         */
        public SimpleExplanation(int systemImage, String explanationText) {
            this(Display.getDefault().getSystemImage(systemImage),
                explanationText);
        }

        /**
         * Constructs a new explanation for use with
         * {@link SimpleExplanationComposite}.
         * 
         * @param explanationImage
         *            Explanatory image {@link SWT#ICON_INFORMATION})
         * @param explanationText
         *            The explanation to be shown next to the image
         */
        public SimpleExplanation(Image explanationImage, String explanationText) {
            this.explanationText = explanationText;
            this.explanationImage = explanationImage;
        }
    }

    protected ScrolledComposite scrolledComposite;
    protected Label explanationLabel;

    /**
     * Constructs a new explanation composite.
     * 
     * @param parent
     *            The parent control
     * @param style
     *            Style constants
     */
    public SimpleExplanationComposite(Composite parent, int style) {
        super(parent, style & ~(SWT.V_SCROLL | SWT.V_SCROLL | SWT.BORDER), null);

        super.setLayout(new FillLayout());

        this.scrolledComposite = new ScrolledComposite(this, style);
        this.scrolledComposite.setExpandHorizontal(true);
        this.scrolledComposite.setExpandVertical(true);

        this.explanationLabel = new Label(this.scrolledComposite, SWT.WRAP);
        this.scrolledComposite.setContent(this.explanationLabel);

        this.scrolledComposite.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                Rectangle clientArea = SimpleExplanationComposite.this.scrolledComposite
                    .getClientArea();

                int verticalBarWidth = ((SimpleExplanationComposite.this.scrolledComposite
                    .getVerticalBar() != null) ? SimpleExplanationComposite.this.scrolledComposite
                    .getVerticalBar().getSize().x : 0);

                Point minSize = explanationLabel.computeSize(clientArea.width
                    - verticalBarWidth, SWT.DEFAULT);

                SimpleExplanationComposite.this.layout();

                scrolledComposite.setMinSize(minSize);
            }
        });
    }

    /**
     * Constructs a new explanation composite.
     * 
     * @param parent
     *            The parent control
     * @param style
     *            Style constants
     * @param simpleExplanation
     *            Explanation to be displayed by the
     *            {@link SimpleExplanationComposite}
     */
    public SimpleExplanationComposite(Composite parent, int style,
        SimpleExplanation simpleExplanation) {
        this(parent, style);
        setExplanation(simpleExplanation);
    }

    /**
     * Sets the explanation text
     * 
     * @param simpleExplanation
     *            Explanation to be displayed by the
     *            {@link SimpleExplanationComposite}
     */
    public void setExplanation(SimpleExplanation simpleExplanation) {
        this.setExplanationImage((simpleExplanation != null) ? simpleExplanation.explanationImage
            : null);
        this.explanationLabel
            .setText((simpleExplanation != null && simpleExplanation.explanationText != null) ? simpleExplanation.explanationText
                : "");
        this.layout();
    }

    @Override
    public void setLayout(Layout layout) {
        // this composite controls its layout itself
    }
}
