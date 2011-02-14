package de.fu_berlin.inf.dpp.ui.widgets.explanation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

import de.fu_berlin.inf.dpp.ui.widgets.MinSizeComposite;

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
 * This class may be sub classed by custom control implementors who are building
 * controls that are constructed from aggregates of other controls.
 * </p>
 * 
 * @see Composite
 * @author bkahlert
 * 
 */
public class ExplanationComposite extends MinSizeComposite {
    /**
     * If set to true a border around the {@link #getClientArea() clientArea} is
     * drawn.
     */
    protected static final boolean DEBUG = false;

    /**
     * The image containing explanatory information.
     */
    protected Image explanationImage;

    /**
     * The space between explanation image and explanation
     */
    protected int spacing = 20;

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
                if (explanationImage == null || explanationImage.isDisposed())
                    return;

                Rectangle clientArea = ExplanationComposite.this.cachedClientArea;
                if (clientArea == null)
                    clientArea = getClientArea();

                if (DEBUG) {
                    e.gc.drawRectangle(clientArea.x, clientArea.y,
                        clientArea.width - 1, clientArea.height - 1);
                }

                Rectangle bounds = ExplanationComposite.this.getBounds();
                int x, y = 0;
                if (ExplanationComposite.this.getChildren().length == 0) {
                    // If no child elements exist, place the icon in the center
                    // of the canvas
                    x = (bounds.width - explanationImage.getBounds().width) / 2;
                    y = (bounds.height - explanationImage.getBounds().height) / 2;
                } else {
                    x = clientArea.x - explanationImage.getBounds().width
                        - getSpacing();
                    y = (bounds.height - explanationImage.getBounds().height) / 2;
                }

                /*
                 * Consider the scroll bars
                 */
                ScrollBar hBar = getHorizontalBar();
                if (hBar != null && hBar.isVisible())
                    y -= hBar.getSize().y / 2;

                e.gc.drawImage(explanationImage, x, y);
            }
        });
    }

    @Override
    public void layout(boolean changed) {
        super.layout(changed);
        if (DEBUG)
            this.redraw();
    }

    /**
     * Returns the space between explanation image and explanation
     * 
     * @return
     */
    public int getSpacing() {
        return spacing;
    }

    /**
     * Sets the spacing between explanation image and explanation
     * 
     * @param spacing
     */
    public void setSpacing(int spacing) {
        this.spacing = spacing;
        this.layout();
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
        Rectangle clientArea = super.getClientArea();

        int extraLeftMargin = getExtraLeftMargin();

        /*
         * Check whether preferred client size fits into the available size; if
         * not re-compute client size with available size.
         */
        Point newClientSize = this
            .computeSizeTrimless(SWT.DEFAULT, SWT.DEFAULT);

        int wHint = SWT.DEFAULT, hHint = SWT.DEFAULT;
        if (newClientSize.x > clientArea.width)
            /*
             * Because newClientSize contains the trim, we have to subtract the
             * trim for wHint.
             */
            wHint = Math.max(clientArea.width - extraLeftMargin, getMinWidth());
        if (newClientSize.y > clientArea.height)
            /*
             * Because newClientSize contains the trim, we have to subtract the
             * trim for hHint.
             */
            hHint = Math.max(clientArea.height, getMinHeight());
        if (wHint != SWT.DEFAULT || hHint != SWT.DEFAULT) {
            newClientSize = this.computeSizeTrimless(wHint, hHint);
        }

        /*
         * Center the unit of explanationImage and child composites.
         */
        clientArea.x += Math.max(
            (clientArea.width - newClientSize.x + extraLeftMargin) / 2,
            extraLeftMargin);
        clientArea.y += Math.max((clientArea.height - newClientSize.y) / 2, 0);

        clientArea.width = newClientSize.x;
        clientArea.height = newClientSize.y;
        this.cachedClientArea = clientArea;

        return clientArea;
    }

    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        if (isNoTrimComputation())
            return new Rectangle(x, y, width, height);

        width += getExtraLeftMargin();

        if (getExplanationImageHeight() > height)
            height = getExplanationImageHeight();

        return super.computeTrim(x, y, width, height);
    }

    /**
     * Gets the extra left margin needed to correctly display the
     * {@link #explanationImage}.
     * 
     * @return
     */
    protected int getExtraLeftMargin() {
        Point explanationImageSize = getExplanationImageSize();
        return explanationImageSize != null ? explanationImageSize.x
            + getSpacing() : 0;
    }

    /**
     * Returns the {@link #explanationImage}'s dimensions.
     * 
     * @return null if explanation image not set
     */
    protected Point getExplanationImageSize() {
        if (this.explanationImage != null
            && !this.explanationImage.isDisposed()) {
            Rectangle bounds = this.explanationImage.getBounds();
            return new Point(bounds.width, bounds.height);
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link #explanationImage}'s height.
     * 
     * @return 0 if explanation image not set
     */
    protected int getExplanationImageHeight() {
        Point getExplanationImageSize = getExplanationImageSize();
        return (getExplanationImageSize != null) ? getExplanationImageSize.y
            : 0;
    }
}
