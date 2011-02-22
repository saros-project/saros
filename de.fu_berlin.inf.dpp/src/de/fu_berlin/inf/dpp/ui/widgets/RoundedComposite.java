package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.util.ColorUtils;

/**
 * Instances of this class are controls which are capable of containing other
 * controls.
 * <p>
 * The composite's content is surrounded by a rounded rectangle if a background
 * is set.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, BORDER and those supported by Composite</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * <p>
 * Note: If you use SEPARATOR the content gets right aligned and the generated
 * space filled with a small horizontal line.
 * </p>
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
public class RoundedComposite extends Composite {
    /**
     * Scale by which the background color's lightness should be modified for
     * use as the border color.
     */
    private static final float BORDER_LIGHTNESS_SCALE = 0.85f;
    public static final int ARC = 15;
    protected static final int LINE_WEIGHT = 1;
    protected boolean isSeparator;
    protected boolean hasBorder;
    protected Color backgroundColor;
    protected Color borderColor;

    public RoundedComposite(Composite parent, int style) {
        super(parent, style & ~(SWT.SEPARATOR | SWT.BORDER));

        /*
         * Checks whether to display as a separator
         */
        isSeparator = ((style & SWT.SEPARATOR) == SWT.SEPARATOR);
        style = style & ~SWT.SEPARATOR;

        /*
         * Checks whether to display a border
         */
        hasBorder = ((style & SWT.BORDER) == SWT.BORDER);
        style = style & ~SWT.BORDER;

        /*
         * Make sure child widgets respect transparency
         */
        this.setBackgroundMode(SWT.INHERIT_DEFAULT);

        /*
         * Updates the rounded rectangle background.
         */
        this.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                if (RoundedComposite.this.backgroundColor == null)
                    return;

                Rectangle bounds = RoundedComposite.this.getBounds();
                Rectangle clientArea = RoundedComposite.this.getClientArea();

                /*
                 * Draws the rounded background
                 */
                e.gc.setBackground(RoundedComposite.this.backgroundColor);
                e.gc.fillRoundRectangle(clientArea.x, clientArea.y,
                    clientArea.width, clientArea.height, ARC, ARC);

                /*
                 * Draws the border
                 */
                if (hasBorder) {
                    if (borderColor == null || borderColor.isDisposed())
                        borderColor = getDisplay().getSystemColor(
                            SWT.COLOR_BLACK);

                    e.gc.setLineWidth(LINE_WEIGHT);
                    e.gc.setForeground(borderColor);
                    e.gc.drawRoundRectangle(clientArea.x, clientArea.y,
                        clientArea.width - LINE_WEIGHT, clientArea.height
                            - LINE_WEIGHT, ARC, ARC);
                }

                /*
                 * If the control shall be displayed as a separator, we draw a
                 * horizontal line
                 */
                if (isSeparator) {
                    e.gc.setLineWidth(LINE_WEIGHT);
                    e.gc.setForeground(RoundedComposite.this.backgroundColor);
                    int top = (bounds.height - LINE_WEIGHT) / 2;
                    e.gc.drawLine(ARC / 2, top, bounds.width - ARC / 2, top);
                }
            }
        });

        /*
         * Set default layout
         */
        this.setLayout(new FillLayout());
    }

    @Override
    public Rectangle getClientArea() {
        Rectangle clientArea = super.getClientArea();

        /*
         * If rendered as a separator compute the minimal width need width to
         * display the contents
         */
        if (isSeparator) {
            int neededWidth = this.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            int reduceBy = clientArea.width - neededWidth;
            clientArea.x += reduceBy;
            clientArea.width -= reduceBy;
        }

        return clientArea;
    }

    @Override
    public void setBackground(Color color) {
        this.backgroundColor = color;
        if (this.borderColor != null && !this.borderColor.isDisposed())
            this.borderColor.dispose();
        this.borderColor = ColorUtils.scaleColorBy(color,
            BORDER_LIGHTNESS_SCALE);
        this.redraw();
    }

    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.borderColor != null && !this.borderColor.isDisposed()) {
            this.borderColor.dispose();
        }
    }
}
