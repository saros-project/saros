/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2010
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
package de.fu_berlin.inf.dpp.videosharing.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Mode;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.activities.KeyPressedVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseClickedVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseClickedVideoActivity.Button;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseWheeledVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.VideoActivity;
import de.fu_berlin.inf.dpp.videosharing.decode.Decoder;
import de.fu_berlin.inf.dpp.videosharing.decode.DecodingStatisticPacket;

/**
 * @author s-lau
 */
@Component(module = "ui")
public class VideoPlayerView extends ViewPart implements VideoDisplay {
    private static Logger log = Logger.getLogger(VideoPlayerView.class);

    @Inject
    protected VideoSharing videoSharing;
    @Inject
    protected Saros saros;

    protected ObservableValue<VideoSharingSession> videoSharingSessionObservable;

    protected IPreferenceStore preferences;
    protected boolean resample = false;
    protected boolean keepAspectRatio = true;

    protected Composite parent;
    protected Canvas canvas = null;
    protected VideoCanvas videoCanvas = null;

    protected CoolBar statusBar = null;
    protected DecodingStatisticPacket lastShownStatus = null;

    /* used labels */
    protected Label fps;
    protected Label bitrate;
    protected Label delay;
    protected Label welcomeMsg;
    // HACK Remove the '\n' characters from this message, this is terrible.
    protected final String WELCOME_MSG = "Choose a participant from the \"Shared Project Session\" view\nand click the \"Share your Screen\" button to initialize\na videosharing connection.";
    protected final String CONNECTED_MSG = "Connected";

    protected Rectangle imageSize;
    protected BufferedImage nextImage = null;

    protected BlockingQueue<BufferedImage> images = new ArrayBlockingQueue<BufferedImage>(
        1);

    protected ObjectOutputStream activityOutput;

    protected Rectangle clientArea;

    public VideoPlayerView() {
        Saros.reinject(this);

        preferences = saros.getPreferenceStore();
        updateResample();
        updateKeepAspectRatio();
        preferences.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
                    PreferenceConstants.PLAYER_RESAMPLE))
                    updateResample();
                if (event.getProperty().equals(
                    PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO))
                    updateKeepAspectRatio();
            }
        });

        this.videoSharingSessionObservable = videoSharing.getSession();
        this.videoSharingSessionObservable
            .add(new ValueChangeListener<VideoSharingSession>() {

                public void setValue(final VideoSharingSession newValue) {
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            if (newValue == null)
                                reset();
                            else if (newValue.getMode().equals(Mode.CLIENT)
                                || newValue.getMode().equals(Mode.LOCAL))
                                initialize();
                        }
                    });
                }
            });
    }

    protected void updateResample() {
        resample = preferences.getBoolean(PreferenceConstants.PLAYER_RESAMPLE);
    }

    protected void updateKeepAspectRatio() {
        keepAspectRatio = preferences
            .getBoolean(PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO);
    }

    /**
     * No explanation on screen sharing view: changes in: createPartControl,
     * initialize, reset FIX: We entered a massage how to use the screen sharing
     * view, which is display when no remote screen session is active
     */

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        welcomeMsg = new Label(parent, SWT.LEFT | SWT.WRAP);
        welcomeMsg.setText(WELCOME_MSG);
        welcomeMsg.pack();

        createCanvas(parent);
        createStatusBar(parent);
        createActionBar();
    }

    private void createCanvas(Composite parent) {
        canvas = new Canvas(parent, SWT.EMBEDDED);
        GridData align = new GridData(SWT.FILL, SWT.FILL, true, true);
        canvas.setLayoutData(align);

        canvas.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                clientArea = canvas.getClientArea();
            }
        });
        clientArea = canvas.getClientArea();
    }

    private void createStatusBar(Composite parent) {
        this.statusBar = new CoolBar(parent, SWT.FLAT | SWT.HORIZONTAL);
        this.statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
            true, false));
        createStatisticBar(statusBar);
        this.statusBar.pack();
    }

    private void createStatisticBar(CoolBar coolBar) {
        this.fps = createLabel(coolBar, "FPS  ", "rendered frames per second");
        this.bitrate = createLabel(coolBar, "BITRATE  ",
            "average video bitrate");
        this.delay = createLabel(coolBar, "DLY",
            "average delay of rendered frames to original video time");
    }

    protected void createActionBar() {
        final IActionBars actionBars = getViewSite().getActionBars();
        final IToolBarManager toolBarManager = actionBars.getToolBarManager();

        toolBarManager.add(new ChangeSourceMode());
        toolBarManager.add(new StopVideoSession());
        toolBarManager.add(new PauseVideoSession());
    }

    protected Label createLabel(CoolBar coolBar, String text, String toolTip) {
        CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
        Label label = new Label(coolBar, SWT.NONE);

        label.setText(text);
        label.setToolTipText(toolTip);
        label.pack();

        Point size = label.getSize();
        size = label.computeSize(size.x, size.y);
        coolItem.setSize(size);
        coolItem.setMinimumSize(size);
        coolItem.setPreferredSize(size);

        coolItem.setControl(label);

        return label;
    }

    @Override
    public void setFocus() {
        if (videoCanvas != null)
            videoCanvas.requestFocus();
    }

    public Dimension getImageDimension(Dimension dimension) {
        if (!resample)
            return null;
        int iWidth = clientArea.width, iHeight = clientArea.height;
        if (keepAspectRatio) {
            double ratioInput = (double) dimension.width / dimension.height;
            double ratioPlayer = (double) clientArea.width / clientArea.height;
            if (ratioInput < ratioPlayer) {
                iHeight = clientArea.height;
                iWidth = (int) (ratioInput * clientArea.height);
            } else {
                iWidth = clientArea.width;
                iHeight = (int) ((1 / ratioInput) * clientArea.width);
            }
        }
        return new Dimension(iWidth, iHeight);
    }

    public void setActivityOutput(ObjectOutputStream out) {
        this.activityOutput = out;
    }

    public void updateImage(BufferedImage image) {
        if (image == null)
            return;
        if (videoCanvas != null)
            videoCanvas.updateImage(image);
        else {
            // NOP, skip until videoCanvas is initialized
        }
    }

    /**
     * @swt
     */

    public void initialize() {
        welcomeMsg.setText(CONNECTED_MSG);
        welcomeMsg.pack();

        if (videoCanvas == null && !canvas.isDisposed())
            videoCanvas = new VideoCanvas(SWT_AWT.new_Frame(canvas));
        canvas.update();
    }

    /**
     * @swt
     */
    public void reset() {
        if (videoCanvas != null)
            videoCanvas.parentFrame.dispose();
        videoCanvas = null;
        canvas.update();
        fps.setText("");
        bitrate.setText("");
        delay.setText("");

        welcomeMsg.setText(WELCOME_MSG);
        welcomeMsg.pack();
    }

    protected void updateStatusbar() {
        VideoSharingSession session = videoSharingSessionObservable.getValue();
        if (session == null)
            return;

        Decoder decoder = session.getDecoder();
        if (decoder == null)
            return;

        DecodingStatisticPacket statistic = decoder.getLastDecodingStatistic();
        if (statistic == null || statistic == lastShownStatus)
            return;

        lastShownStatus = statistic;

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (!parent.isDisposed()) {
                    fps.setText(String.valueOf(lastShownStatus.getFps()));
                    bitrate.setText(Util.formatByte(lastShownStatus.getBytes()));
                    delay.setText(String.valueOf(lastShownStatus.getDelay()));
                }
            }
        });

    }

    class ChangeSourceMode extends Action {

        public ChangeSourceMode() {
            setToolTipText("Change mode of image source");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/switch_mode.gif"));

            videoSharingSessionObservable
                .addAndNotify(new ValueChangeListener<VideoSharingSession>() {

                    public void setValue(VideoSharingSession newValue) {
                        setEnabled(newValue != null
                            && (newValue.getMode().equals(Mode.CLIENT) || newValue
                                .getMode().equals(Mode.LOCAL)));
                    }
                });
        }

        @Override
        public void run() {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    VideoSharingSession session = VideoPlayerView.this.videoSharingSessionObservable
                        .getValue();
                    if (session != null) {
                        session.requestChangeImageSourceMode();
                    }
                }
            });
        }
    }

    class StopVideoSession extends Action {

        public StopVideoSession() {
            setToolTipText("Stop running session");
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_ELCL_STOP));

            videoSharingSessionObservable
                .addAndNotify(new ValueChangeListener<VideoSharingSession>() {

                    public void setValue(VideoSharingSession newValue) {
                        setEnabled(newValue != null
                            && (newValue.getMode().equals(Mode.CLIENT) || newValue
                                .getMode().equals(Mode.LOCAL)));
                    }
                });
        }

        @Override
        public void run() {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    VideoSharingSession session = VideoPlayerView.this.videoSharingSessionObservable
                        .getValue();
                    if (session != null) {
                        session.requestStop();
                        setEnabled(false);
                    }
                }
            });
        }

    }

    class PauseVideoSession extends Action {

        public PauseVideoSession() {
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/suspend_co.gif"));
            setToolTipText("Pause");
            setEnabled(false);
            videoSharingSessionObservable
                .add(new ValueChangeListener<VideoSharingSession>() {

                    public void setValue(VideoSharingSession newValue) {
                        updateState();
                    }
                });
        }

        @Override
        public void run() {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    VideoSharingSession session = VideoPlayerView.this.videoSharingSessionObservable
                        .getValue();
                    if (session != null) {
                        session.pause();
                        updateState();
                    }
                }
            });
        }

        protected void updateState() {
            VideoSharingSession session = videoSharingSessionObservable
                .getValue();
            boolean enabled = session != null
                && (session.getMode().equals(Mode.CLIENT) || session.getMode()
                    .equals(Mode.LOCAL));
            setEnabled(enabled);
            if (enabled && session != null) {
                if (session.isPaused()) {
                    setImageDescriptor(SarosUI
                        .getImageDescriptor("icons/resume_co.gif"));
                    setToolTipText("Resume");
                } else {
                    setImageDescriptor(SarosUI
                        .getImageDescriptor("icons/suspend_co.gif"));
                    setToolTipText("Pause");
                }
            } else {
                setImageDescriptor(SarosUI
                    .getImageDescriptor("icons/suspend_co.gif"));
                setToolTipText(null);
            }
        }
    }

    /**
     * Canvas for displaying video, inspired by <a
     * href="http://www.dreamincode.net/forums/showtopic113451.htm">Java Game
     * (Actually the most efficient way to repaint)</a>
     * 
     * @author s-lau
     */
    protected class VideoCanvas extends java.awt.Canvas implements VideoDisplay {
        private static final long serialVersionUID = 3182304031585212029L;

        protected Dimension lastImageDimension = null;
        protected Frame parentFrame;

        protected boolean paintInProgress = false;

        protected VideoCanvas(Frame parent) {
            this.parentFrame = parent;
            setBackground(Color.BLACK);
            setIgnoreRepaint(true);

            parent.add(this);
            parent.setVisible(true);
            this.createBufferStrategy(2);
            parent.validate();

            addMouseListener(new ActivityMouseClickListener());
            addMouseWheelListener(new ActivityMouseWheelListener());
            addKeyListener(new ActivityKeyboardListener());
        }

        public void setActivityOutput(ObjectOutputStream out) {
            activityOutput = out;
        }

        public Dimension getImageDimension(Dimension dimenesion) {
            return null;
        }

        public void updateImage(BufferedImage image) {
            updateImage(image, 0, 0);
        }

        public void updateImage(BufferedImage tile, int x, int y) {
            if (paintInProgress) {
                log.warn("Rendering seems too slow.");
                return;
            }
            paintInProgress = true;

            lastImageDimension = new Dimension(tile.getWidth(),
                tile.getHeight());
            Dimension size = new Dimension(clientArea.width, clientArea.height);

            setSize(size);
            parentFrame.setSize(size);

            BufferStrategy strategy = getBufferStrategy();
            Graphics graphics = strategy.getDrawGraphics();

            graphics.fillRect(0, 0, clientArea.width, clientArea.height);
            graphics.drawImage(tile, x, y, null);

            graphics.dispose();

            strategy.show();
            Toolkit.getDefaultToolkit().sync();

            paintInProgress = false;

            updateStatusbar();
        }

        protected void sendActivity(VideoActivity activity) {
            if (activityOutput == null)
                return;

            try {
                activityOutput.writeObject(activity);
            } catch (IOException e) {
                log.warn("Could not send activity: ", e);
            }
        }

        public void reset() {
            // nothing to do
        }

        public void initialize() {
            // nothing to do
        }

        protected class ActivityMouseClickListener implements MouseListener {

            public void mouseClicked(MouseEvent e) {
                if (lastImageDimension == null)
                    return;

                Button pressedButton = null;

                switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    pressedButton = Button.LEFT;
                    break;
                case MouseEvent.BUTTON2:
                    pressedButton = Button.MIDDLE;
                    break;
                case MouseEvent.BUTTON3:
                    pressedButton = Button.RIGHT;
                    break;
                default:
                    pressedButton = Button.NONE;
                }

                sendActivity(new MouseClickedVideoActivity(e.getX(), e.getY(),
                    lastImageDimension.width, lastImageDimension.height,
                    pressedButton));
            }

            public void mouseEntered(MouseEvent e) {
                // NOP
            }

            public void mouseExited(MouseEvent e) {
                // NOP
            }

            public void mousePressed(MouseEvent e) {
                // NOP
            }

            public void mouseReleased(MouseEvent e) {
                // NOP
            }

        }

        protected class ActivityMouseWheelListener implements
            MouseWheelListener {

            public void mouseWheelMoved(MouseWheelEvent e) {
                if (lastImageDimension == null)
                    return;

                sendActivity(new MouseWheeledVideoActivity(e.getX(), e.getY(),
                    lastImageDimension.width, lastImageDimension.height,
                    e.getWheelRotation()));
            }

        }

        protected class ActivityKeyboardListener implements KeyListener {

            public void keyPressed(KeyEvent e) {
                // NOP
            }

            public void keyReleased(KeyEvent e) {
                // NOP
            }

            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c != KeyEvent.CHAR_UNDEFINED)
                    sendActivity(new KeyPressedVideoActivity(c));
            }

        }
    }

}
