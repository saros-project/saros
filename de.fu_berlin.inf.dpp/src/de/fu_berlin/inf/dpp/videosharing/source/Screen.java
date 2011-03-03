/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.videosharing.source;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import com.thoughtworks.xstream.InitializationException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.activities.KeyPressedVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.VideoActivity;

/**
 * Responsible for taking screenshots.
 * 
 * @author s-lau
 * 
 */
public class Screen implements ImageSource {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(Screen.class);

    @Inject
    protected Saros saros;
    protected IPreferenceStore preferences;

    protected InfoText infoText = new InfoText();

    /**
     * mode this instance uses for taking screenshots
     */
    protected Mode mode;

    public static enum Mode {
        FOLLOW_MOUSE, FULL_SCREEN
    }

    /**
     * defines the area of the current screen (now only primary screen) which is
     * captured. can be smaller than fullScreenArea for zooming.
     */
    protected Rectangle screenArea = new Rectangle(Toolkit.getDefaultToolkit()
        .getScreenSize());

    /**
     * whole area of current screen (resolution)
     */
    protected Dimension fullScreenArea = Toolkit.getDefaultToolkit()
        .getScreenSize();

    /**
     * area around the mousepointer which is beeing captured when followMouse is
     * true
     */
    protected Dimension mouseArea;

    protected boolean showMousePointer;

    /**
     * the position of mousepointer at the last screenshot in followmode. should
     * only be updated when {@link #lastMouseRectangle} is updated.
     */
    protected Point lastMousePosition = null;

    /**
     * last capturearea in followmode
     */
    protected Rectangle lastMouseRectangle = null;

    protected Robot robot = null;

    /**
     * @throws InitializationException
     *             Could not create screen-grabber
     */
    public Screen(VideoSharingSession videoSharingSession)
        throws InitializationException {
        SarosPluginContext.initComponent(this);
        preferences = saros.getPreferenceStore();
        mode = Enum.valueOf(Mode.class, preferences
            .getString(PreferenceConstants.SCREEN_INITIAL_MODE));
        mouseArea = new Dimension(Math.min(preferences
            .getInt(PreferenceConstants.SCREEN_MOUSE_AREA_WIDTH),
            fullScreenArea.width), Math.min(preferences
            .getInt(PreferenceConstants.SCREEN_MOUSE_AREA_HEIGHT),
            fullScreenArea.height));

        showMousePointer = preferences
            .getBoolean(PreferenceConstants.SCREEN_SHOW_MOUSEPOINTER);

        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            throw new InitializationException("Could not setup screengrabber",
                e);
        }
    }

    public synchronized BufferedImage toImage() {
        final BufferedImage capture = robot
            .createScreenCapture(followsMouse() ? mouseRectangle() : screenArea);

        String text = infoText.getText();

        if (text != null) {
            // get graphic to paint on
            Graphics2D pic = capture.createGraphics();

            try {
                int height = Math.max(15, capture.getHeight() / 20);

                // red infotext
                pic.setColor(Color.RED);
                pic.setFont(pic.getFont().deriveFont((float) (height))
                    .deriveFont(Font.BOLD));
                pic.drawString(text, 0, height);

            } finally {
                pic.dispose();
            }
        }

        if (showMousePointer) {
            Graphics2D graphics2d = capture.createGraphics();

            int mouseX = 0, mouseY = 0;
            double heightStretch = 0, widthStretch = 0;
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            switch (mode) {
            case FULL_SCREEN:
                mouseX = mousePoint.x;
                mouseY = mousePoint.y;
                heightStretch = fullScreenArea.height;
                widthStretch = fullScreenArea.width;
                break;
            case FOLLOW_MOUSE:
                mouseX = Math.min(mouseArea.width / 2, mousePoint.x);
                mouseY = Math.min(mouseArea.height / 2, mousePoint.y);
                if ((fullScreenArea.width - mouseArea.width / 2) < mousePoint.x)
                    mouseX = mouseArea.width - fullScreenArea.width
                        + mousePoint.x;
                if ((fullScreenArea.height - mouseArea.height / 2) < mousePoint.y)
                    mouseY = mouseArea.height - fullScreenArea.height
                        + mousePoint.y;
                heightStretch = mouseArea.height;
                widthStretch = mouseArea.width;
                break;
            }

            // stretch pointer on big screens to see it
            heightStretch = Math.max(200, heightStretch) / 200;
            widthStretch = Math.max(200, widthStretch) / 200;

            Polygon mousePointer = new Polygon();
            mousePointer.addPoint(mouseX, mouseY);
            mousePointer.addPoint(mouseX, (int) (mouseY + 17 * heightStretch));
            mousePointer.addPoint((int) (mouseX + 10 * widthStretch),
                (int) (mouseY + 12 * heightStretch));
            // mousePointer.addPoint((int) (mouseX + 12 * widthStretch),
            // (int) (mouseY + 10 * widthStretch));

            graphics2d.setColor(Color.WHITE);
            graphics2d.fill(mousePointer);

            graphics2d.setColor(Color.BLACK);
            graphics2d.draw(mousePointer);

            graphics2d.dispose();
        }

        return capture;
    }

    protected Rectangle mouseRectangle() {
        if (MouseInfo.getPointerInfo() == null) {
            // sometimes a pointer is not available
            return updateMouseRectangle(new Point());
        }
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        return updateMouseRectangle(mousePosition);
    }

    protected Rectangle updateMouseRectangle(Point mousePosition) {
        Point newEdge = new Point(mousePosition.x - mouseArea.width / 2,
            mousePosition.y - mouseArea.height / 2);

        // normalize edge
        if (newEdge.x < 0)
            newEdge.x = 0;
        if (newEdge.y < 0)
            newEdge.y = 0;

        if (newEdge.x + mouseArea.width > fullScreenArea.getWidth())
            newEdge.x = (int) fullScreenArea.getWidth() - mouseArea.width;
        if (newEdge.y + mouseArea.height > fullScreenArea.getHeight())
            newEdge.y = (int) fullScreenArea.getHeight() - mouseArea.height;

        lastMouseRectangle = new Rectangle(newEdge, mouseArea);
        return lastMouseRectangle;
    }

    public boolean followsMouse() {
        return mode == Mode.FOLLOW_MOUSE;
    }

    public void setMode(Mode newMode) {
        this.mode = newMode;
    }

    public void processActivity(VideoActivity activity) {
        switch (activity.getType()) {
        case MOUSE_CLICK:
            switchMode();
            break;
        case KEY_PRESSED:
            String info = infoText.getText();
            if (activity instanceof KeyPressedVideoActivity) {
                KeyPressedVideoActivity key = (KeyPressedVideoActivity) activity;
                if (info == null)
                    info = "";
                infoText.setText(info + key.typedChar);
            }
            break;
        default:
            break;
        }
    }

    public void switchMode() {
        if (mode == Mode.FULL_SCREEN) {
            mode = Mode.FOLLOW_MOUSE;
        } else {
            mode = Mode.FULL_SCREEN;
        }
        infoText.setText(mode.name());
    }

    public void dispose() {
        // nothing to dispose
    }

    /**
     * Holds a {@link String} and returns it for {@link #SHOW_TIME} seconds. It
     * can only hold one {@link String} at a time.
     */
    static class InfoText {
        public static final int SHOW_TIME = 3;
        String text = null;
        Long until = null;

        public synchronized String getText() {
            if (text != null && until != null
                && until > System.currentTimeMillis()) {
                return text;
            }
            text = null;
            return null;
        }

        public synchronized void setText(String text) {
            this.text = text;
            this.until = System.currentTimeMillis() + SHOW_TIME * 1000;
        }
    }

}
