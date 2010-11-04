package de.fu_berlin.inf.dpp.ui.widgets.explanation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This composite displays a descriptive icon and a {@link Control} that
 * displays some information / explication. <br/>
 * Use this composite like every other composite, especially by setting the
 * layout and adding sub {@link Control}s.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by Composite</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * <p>
 * This class may be subclassed by custom control implementors who are building
 * controls that are constructed from aggregates of other controls.
 * </p>
 * 
 * @see Composite
 * @author bkahlert
 * 
 */
public class ExplanationComposite extends Composite {
    public static final int MARGIN_WIDTH = 15;
    public static final int MARGIN_HEIGHT = 10;
    public static final int SPACING = 20;

    /**
     * The image containing explanatory information.
     */
    protected Image explanationImage;

    /**
     * The minimal size
     */
    protected Point minSize;

    /**
     * Used by the paint listener to correctly place the backgroundImage.
     */
    protected Rectangle cachedClientArea;

    /**
     * Constructs a new explanation composite.
     * 
     * @param parent
     *            The parent control
     * @param style
     *            Style constants
     * @param systemImage
     *            SWT constant that declares a system image (e.g.
     *            {@link SWT#ICON_INFORMATION})
     */
    public ExplanationComposite(Composite parent, int style, int systemImage) {
        this(parent, style, parent.getDisplay().getSystemImage(systemImage));
    }

    /**
     * Constructs a new explanation composite.
     * 
     * @param parent
     *            The parent control
     * @param style
     *            Style constants
     * @param explanationImage
     *            Explanatory image
     */
    public ExplanationComposite(Composite parent, int style,
        Image explanationImage) {
        super(parent, style);

        setExplanationImage(explanationImage);

        /*
         * Make sure child widgets respect transparency
         */
        this.setBackgroundMode(SWT.INHERIT_DEFAULT);

        /*
         * Adds the explanation icon to the left of the explanation.
         */
        this.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Image explanationImage = ExplanationComposite.this.explanationImage;
                if (explanationImage == null)
                    return;

                Rectangle clientArea = ExplanationComposite.this.cachedClientArea;
                if (clientArea == null)
                    clientArea = getClientArea();

                Rectangle bounds = ExplanationComposite.this.getBounds();
                int x, y = 0;
                if (ExplanationComposite.this.getChildren().length == 0) {
                    // If no child elements exist, place the icon in the center
                    // of the canvas
                    x = (bounds.width - explanationImage.getBounds().width) / 2;
                    y = (bounds.height - explanationImage.getBounds().height) / 2;
                } else {
                    // If child elements exist, place the icon to their left
                    x = clientArea.x - explanationImage.getBounds().width
                        - SPACING;
                    y = clientArea.y + clientArea.height / 2
                        - explanationImage.getBounds().height / 2;
                }
                e.gc.drawImage(explanationImage, x, y);
            }
        });
    }

    /**
     * Returns the minimal size
     * 
     * @return
     */
    public Point getMinSize() {
        return minSize;
    }

    /**
     * Sets the minimal size.
     * <p>
     * If the parent composite is smaller it won't affect the explanation
     * composite's size anymore.
     * 
     * @param minSize
     *            for this composite; if you only want to set one dimension, set
     *            {@link SWT#DEFAULT} for the other
     */
    public void setMinSize(Point minSize) {
        this.minSize = minSize;
    }

    /**
     * Sets the explanation image on the base of an SWT constant
     * 
     * @param systemImage
     *            SWT constant that declares a system image (e.g.
     *            {@link SWT#ICON_INFORMATION})
     */
    public void setExplanationImage(int systemImage) {
        setExplanationImage(this.getDisplay().getSystemImage(systemImage));
    }

    /**
     * Sets the explanation image on the base of an {@link Image}
     * <p>
     * Note: It is the caller's responsibility to dispose the explanationImage
     * correctly. <br>
     * You must not dispose the explanationImage before the this
     * {@link ExplanationComposite} is disposed.
     * 
     * @param explanationImage
     *            Explanatory image
     * 
     */
    public void setExplanationImage(Image explanationImage) {
        this.explanationImage = explanationImage;
        layout();
        redraw();
    }

    @Override
    public Rectangle getClientArea() {
        int imageWidth = ((explanationImage != null) ? SPACING
            + explanationImage.getBounds().width : 0);

        /*
         * Does consider eventually limited clientArea
         */
        Rectangle clientArea = super.getClientArea();

        clientArea.width -= 2 * MARGIN_WIDTH + imageWidth;

        /*
         * Check whether preferred client size fits into the available width; if
         * not re-compute client size with limited width.
         */
        Point preferredClientSize = this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (preferredClientSize.x <= clientArea.width) {
            clientArea.width = preferredClientSize.x;
            clientArea.height = Math.min(clientArea.height,
                preferredClientSize.y);
        } else {
            Point limitedClientSize = this.computeSize(clientArea.width,
                SWT.DEFAULT);
            clientArea.width = Math.min(clientArea.width, limitedClientSize.x);
            clientArea.height = Math
                .min(clientArea.height, limitedClientSize.y);
        }

        /*
         * Respect the margins
         */
        if (clientArea.width > super.getClientArea().width - 2 * MARGIN_WIDTH) {
            clientArea.width = super.getClientArea().width - 2 * MARGIN_WIDTH;
        }

        if (clientArea.height > super.getClientArea().height - 2
            * MARGIN_HEIGHT) {
            clientArea.height = super.getClientArea().height - 2
                * MARGIN_HEIGHT;
        }

        /*
         * Respect the minimal size
         */
        if (this.minSize != null) {
            if (this.minSize.x != SWT.DEFAULT
                && clientArea.width < this.minSize.x)
                clientArea.width = this.minSize.x;
            if (this.minSize.y != SWT.DEFAULT
                && clientArea.height < this.minSize.y)
                clientArea.height = this.minSize.y;
        }

        /*
         * Center the unit of explanationImage and child composites.
         */
        clientArea.x += (this.getBounds().width - clientArea.width + imageWidth) / 2;
        clientArea.y = Math.max(clientArea.y,
            (this.getBounds().height - clientArea.height) / 2);

        /*
         * Don't allow negative coordinates
         */
        int minX = MARGIN_WIDTH + imageWidth;
        int minY = 0;
        if (clientArea.x < minX)
            clientArea.x = minX;
        if (clientArea.y < minY)
            clientArea.y = minY;

        this.cachedClientArea = clientArea;

        return clientArea;
    }
}
