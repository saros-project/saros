package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * This composite implements a generic scroll behavior.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>{@link SWT#H_SCROLL}, {@link SWT#V_SCROLL} - these styles do not only
 * display scroll bars but make them also work</dd>
 * <dd>Styles supported by {@link Composite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * <p>
 * This class may be sub-classed by custom control implementors who are building
 * controls that are constructed from aggregates of other controls.
 * </p>
 * 
 * @see Composite
 * @author bkahlert
 * 
 */
public class ScrollableComposite extends MinSizeComposite {

    /**
     * Constructs a new {@link ScrollableComposite}.
     * 
     * @param parent
     *            The parent control
     * @param style
     *            Style constants
     */
    public ScrollableComposite(Composite parent, final int style) {
        super(parent, style);
        this.setForeground(parent.getForeground());

        final ScrollBar hBar = getHorizontalBar();
        final ScrollBar vBar = getVerticalBar();

        if (hBar != null || vBar != null) {
            this.addListener(SWT.Resize, new Listener() {
                public void handleEvent(Event e) {
                    Rectangle clientArea = getClientArea();

                    int width = clientArea.x + clientArea.width;
                    int height = clientArea.y + clientArea.height;

                    Point neededSize = null;
                    if (vBar != null && hBar != null) {
                        neededSize = computeSizeTrimless(width, SWT.DEFAULT);
                        // remove the vertical trim
                        neededSize.y -= determineTrim().y;
                    } else if (vBar != null) {
                        neededSize = computeSizeTrimless(width, SWT.DEFAULT);
                        // remove the vertical trim
                        neededSize.y -= determineTrim().y;
                    } else if (hBar != null) {
                        neededSize = computeSizeTrimless(SWT.DEFAULT, height);
                        // remove the horizontal trim
                        neededSize.x -= determineTrim().x;
                    }

                    if (neededSize == null)
                        return;

                    if (hBar != null) {
                        if (neededSize.x > width) {
                            hBar.setMaximum(neededSize.x);
                            hBar.setThumb(width);
                            hBar.setEnabled(true);
                            hBar.setVisible(true);
                        } else {
                            hBar.setVisible(false);
                            hBar.setEnabled(false);
                            hBar.setSelection(0);
                        }
                    }

                    if (vBar != null) {
                        if (neededSize.y > height) {
                            vBar.setMaximum(neededSize.y);
                            vBar.setThumb(height);
                            vBar.setEnabled(true);
                            vBar.setVisible(true);
                        } else {
                            vBar.setVisible(false);
                            vBar.setEnabled(false);
                            vBar.setSelection(0);
                        }
                    }
                    layout();
                }
            });

            if (hBar != null)
                hBar.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        layout();
                    }
                });

            if (vBar != null)
                vBar.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        layout();
                    }
                });
        }

        /*
         * Make sure child widgets respect transparency
         */
        this.setBackgroundMode(SWT.INHERIT_DEFAULT);
    }

    @Override
    public Rectangle getClientArea() {
        Rectangle clientArea = super.getClientArea();

        ScrollBar vBar = this.getVerticalBar(), hBar = this.getHorizontalBar();
        if (hBar != null) {
            clientArea.x -= hBar.getSelection();
            clientArea.width += hBar.getSelection();
        }
        if (vBar != null) {
            clientArea.y -= vBar.getSelection();
            clientArea.height += vBar.getSelection();
        }

        return clientArea;
    }
}
