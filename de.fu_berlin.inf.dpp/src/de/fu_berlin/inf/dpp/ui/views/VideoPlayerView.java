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
package de.fu_berlin.inf.dpp.ui.views;

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
import java.text.MessageFormat;

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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Mode;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.activities.KeyPressedVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseClickedVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseClickedVideoActivity.Button;
import de.fu_berlin.inf.dpp.videosharing.activities.MouseWheeledVideoActivity;
import de.fu_berlin.inf.dpp.videosharing.activities.VideoActivity;
import de.fu_berlin.inf.dpp.videosharing.decode.Decoder;
import de.fu_berlin.inf.dpp.videosharing.decode.DecodingStatisticPacket;
import de.fu_berlin.inf.dpp.videosharing.player.VideoDisplay;
import de.fu_berlin.inf.nebula.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.nebula.explanation.explanatory.ListExplanatoryViewPart;

/**
 * @author s-lau
 * @author bkahlert (ExplanatoryViewPart)
 */
@Component(module = "ui")
public class VideoPlayerView extends ListExplanatoryViewPart implements
    VideoDisplay {
    private static final Logger log = Logger.getLogger(VideoPlayerView.class);

    public static final String ID = "de.fu_berlin.inf.dpp.ui.views.VideoPlayerView"; //$NON-NLS-1$

    @Inject
    protected Saros saros;
    @Inject
    protected VideoSessionObservable videoSharingSessionObservable;

    protected IPreferenceStore preferences;
    protected boolean resample = false;
    protected boolean keepAspectRatio = true;

    /* howto */
    protected ListExplanation howTo = new ListExplanation(SWT.ICON_INFORMATION,
        Messages.VideoPlayerView_howTo_intro,
        Messages.VideoPlayerView_howTo_step_1,
        Messages.VideoPlayerView_howTo_step_2, MessageFormat.format(
            Messages.VideoPlayerView_howTo_step_3,
            VideoSharingAction.TOOLTIP_START_SESSION));

    /* content */
    protected Composite parent;
    protected Canvas canvas = null;
    protected VideoCanvas videoCanvas = null;

    protected CoolBar statusBar = null;
    protected DecodingStatisticPacket lastShownStatus = null;

    /* used labels */
    protected Label fps;
    protected Label bitrate;
    protected Label delay;

    protected ObjectOutputStream activityOutput;

    protected Rectangle clientArea;

    public VideoPlayerView() {
        SarosPluginContext.initComponent(this);

        preferences = saros.getPreferenceStore();
        updateResample();
        updateKeepAspectRatio();
        preferences.addPropertyChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
                    PreferenceConstants.PLAYER_RESAMPLE))
                    updateResample();
                if (event.getProperty().equals(
                    PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO))
                    updateKeepAspectRatio();
            }
        });

        this.videoSharingSessionObservable
            .add(new ValueChangeListener<VideoSharingSession>() {

                @Override
                public void setValue(final VideoSharingSession newValue) {
                    SWTUtils.runSafeSWTSync(log, new Runnable() {
                        @Override
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

    @Override
    public void createContentPartControl(Composite parent) {
        this.showExplanation(this.howTo);

        this.parent = parent;
        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        createCanvas(parent);
        createStatusBar(parent);
        createActionBar();
    }

    private void createCanvas(Composite parent) {
        canvas = new Canvas(parent, SWT.EMBEDDED);
        GridData align = new GridData(SWT.FILL, SWT.FILL, true, true);
        canvas.setLayoutData(align);

        canvas.addListener(SWT.Resize, new Listener() {

            @Override
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
        this.fps = createLabel(coolBar, Messages.VideoPlayerView_fps,
            Messages.VideoPlayerView_fps_text);
        this.bitrate = createLabel(coolBar, Messages.VideoPlayerView_bitrate,
            Messages.VideoPlayerView_bitrate_text);
        this.delay = createLabel(coolBar, Messages.VideoPlayerView_dly,
            Messages.VideoPlayerView_dly_text);
    }

    protected void createActionBar() {
        final IActionBars actionBars = getViewSite().getActionBars();
        final IToolBarManager toolBarManager = actionBars.getToolBarManager();

        toolBarManager.add(new ChangeSourceModeAction());
        toolBarManager.add(new StopVideoSessionAction());
        toolBarManager.add(new PauseVideoSessionAction());
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

    @Override
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

    @Override
    public void setActivityOutput(ObjectOutputStream out) {
        this.activityOutput = out;
    }

    @Override
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
    @Override
    public void initialize() {
        if (videoCanvas == null && !canvas.isDisposed())
            videoCanvas = new VideoCanvas(SWT_AWT.new_Frame(canvas));
        canvas.update();
        this.hideExplanation();
    }

    /**
     * @swt
     */
    @Override
    public void reset() {
        this.showExplanation(this.howTo);
        if (videoCanvas != null)
            videoCanvas.parentFrame.dispose();
        videoCanvas = null;
        canvas.update();
        fps.setText(""); //$NON-NLS-1$
        bitrate.setText(""); //$NON-NLS-1$
        delay.setText(""); //$NON-NLS-1$
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

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                if (!parent.isDisposed()) {
                    fps.setText(String.valueOf(lastShownStatus.getFps()));
                    bitrate.setText(Utils.formatByte(lastShownStatus.getBytes()));
                    delay.setText(String.valueOf(lastShownStatus.getDelay()));
                }
            }
        });

    }

    class ChangeSourceModeAction extends Action {

        public ChangeSourceModeAction() {
            setToolTipText(Messages.VideoPlayerView_change_image_source);
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/chvdsrc.png")); //$NON-NLS-1$

            videoSharingSessionObservable
                .addAndNotify(new ValueChangeListener<VideoSharingSession>() {

                    @Override
                    public void setValue(VideoSharingSession newValue) {
                        setEnabled(newValue != null
                            && (newValue.getMode().equals(Mode.CLIENT) || newValue
                                .getMode().equals(Mode.LOCAL)));
                    }
                });
        }

        @Override
        public void run() {
            Utils.runSafeAsync(log, new Runnable() {
                @Override
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

    class StopVideoSessionAction extends Action {

        public StopVideoSessionAction() {
            setToolTipText(Messages.VideoPlayerView_stop_session);
            setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_ELCL_STOP));

            videoSharingSessionObservable
                .addAndNotify(new ValueChangeListener<VideoSharingSession>() {

                    @Override
                    public void setValue(VideoSharingSession newValue) {
                        setEnabled(newValue != null
                            && (newValue.getMode().equals(Mode.CLIENT) || newValue
                                .getMode().equals(Mode.LOCAL)));
                    }
                });
        }

        @Override
        public void run() {
            Utils.runSafeAsync(log, new Runnable() {
                @Override
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

    class PauseVideoSessionAction extends Action {

        public PauseVideoSessionAction() {
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/pausevideo.gif")); //$NON-NLS-1$
            setToolTipText(Messages.VideoPlayerView_pause);
            setEnabled(false);
            videoSharingSessionObservable
                .add(new ValueChangeListener<VideoSharingSession>() {

                    @Override
                    public void setValue(VideoSharingSession newValue) {
                        updateState();
                    }
                });
        }

        @Override
        public void run() {
            Utils.runSafeAsync(log, new Runnable() {
                @Override
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
                    setImageDescriptor(ImageManager
                        .getImageDescriptor("icons/elcl16/resumevideo.gif")); //$NON-NLS-1$
                    setToolTipText(Messages.VideoPlayerView_resume);
                } else {
                    setImageDescriptor(ImageManager
                        .getImageDescriptor("icons/elcl16/pausevideo.gif")); //$NON-NLS-1$
                    setToolTipText(Messages.VideoPlayerView_pause);
                }
            } else {
                setImageDescriptor(ImageManager
                    .getImageDescriptor("icons/elcl16/pausevideo.gif")); //$NON-NLS-1$
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

        @Override
        public void setActivityOutput(ObjectOutputStream out) {
            activityOutput = out;
        }

        @Override
        public Dimension getImageDimension(Dimension dimenesion) {
            return null;
        }

        @Override
        public void updateImage(BufferedImage image) {
            updateImage(image, 0, 0);
        }

        public void updateImage(BufferedImage tile, int x, int y) {
            if (paintInProgress) {
                log.warn(Messages.VideoPlayerView_rendering_too_slow);
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
                log.warn("Could not send activity: ", e); //$NON-NLS-1$
            }
        }

        @Override
        public void reset() {
            // nothing to do
        }

        @Override
        public void initialize() {
            // nothing to do
        }

        protected class ActivityMouseClickListener implements MouseListener {

            @Override
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

            @Override
            public void mouseEntered(MouseEvent e) {
                // NOP
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // NOP
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // NOP
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // NOP
            }

        }

        protected class ActivityMouseWheelListener implements
            MouseWheelListener {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (lastImageDimension == null)
                    return;

                sendActivity(new MouseWheeledVideoActivity(e.getX(), e.getY(),
                    lastImageDimension.width, lastImageDimension.height,
                    e.getWheelRotation()));
            }

        }

        protected class ActivityKeyboardListener implements KeyListener {

            @Override
            public void keyPressed(KeyEvent e) {
                // NOP
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // NOP
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c != KeyEvent.CHAR_UNDEFINED)
                    sendActivity(new KeyPressedVideoActivity(c));
            }

        }
    }

}
