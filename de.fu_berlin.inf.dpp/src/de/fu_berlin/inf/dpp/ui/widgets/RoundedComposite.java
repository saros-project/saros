package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.util.ColorUtils;
import de.fu_berlin.inf.dpp.ui.util.PaintUtils;

/**
 * Instances of this class are controls which are capable of containing other
 * controls.
 * <p>
 * The composite's content is surrounded by a rounded rectangle if a background
 * is set.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, and those supported by Composite excluding NO_BACKGROUND</dd>
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
public class RoundedComposite extends Canvas {
    /**
     * style constants not passed to parent constructor
     */
    protected static final int STYLES = SWT.SEPARATOR | SWT.BORDER;

    protected int style;

    /**
     * Scale by which the background color's lightness should be modified for
     * use as the border color.
     */
    private static final float BORDER_LIGHTNESS_SCALE = 0.85f;

    protected Color border;

    private final Composite parent;

    public RoundedComposite(Composite parent, final int style) {
        super(parent, (style | SWT.NO_BACKGROUND) & ~STYLES);
        this.style = style & STYLES;
        this.parent = parent;

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                paint(e);
            }
        });

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                disposeBorder();
            }
        });

        /*
         * Set default layout
         */
        setLayout(new FillLayout());
    }

    @Override
    public Rectangle getClientArea() {
        Rectangle clientArea = super.getClientArea();

        /*
         * If rendered as a separator compute the minimal width need width to
         * display the contents
         */
        if ((style & SWT.SEPARATOR) != 0) {
            int neededWidth = computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            int reduceBy = clientArea.width - neededWidth;
            clientArea.x += reduceBy;
            clientArea.width -= reduceBy;
        }

        return clientArea;
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        createBorder();
        redraw();
    }

    private void disposeBorder() {
        if (border != null && !border.isDisposed())
            border.dispose();

        border = null;
    }

    private void createBorder() {
        disposeBorder();
        border = ColorUtils.scaleColorBy(getBackground(),
            BORDER_LIGHTNESS_SCALE);
    }

    /**
     * Updates the rounded rectangle background.
     */
    private void paint(final PaintEvent e) {

        final Color backgroundToUse = getBackground();
        final Color borderToUser = border != null ? border : getDisplay()
            .getSystemColor(SWT.COLOR_BLACK);

        final Rectangle bounds = getBounds();
        final Rectangle clientArea = getClientArea();

        final GC gc = e.gc;

        gc.setBackground(parent.getBackground());
        gc.fillRectangle(new Rectangle(e.x, e.y, e.width, e.height));

        PaintUtils.drawRoundedRectangle(gc, clientArea, backgroundToUse);

        if ((style & SWT.BORDER) != 0)
            PaintUtils.drawRoundedBorder(gc, clientArea, borderToUser);

        /*
         * If the control shall be displayed as a separator, we draw a
         * horizontal line
         */

        // FIXME why is this using constants of another util class ?
        if ((style & SWT.SEPARATOR) != 0) {
            final int top = (bounds.height - PaintUtils.LINE_WEIGHT) / 2;

            gc.setLineWidth(PaintUtils.LINE_WEIGHT);
            gc.setForeground(backgroundToUse);
            gc.drawLine(PaintUtils.ARC / 2, top, bounds.width - PaintUtils.ARC
                / 2, top);
        }
    }
}
