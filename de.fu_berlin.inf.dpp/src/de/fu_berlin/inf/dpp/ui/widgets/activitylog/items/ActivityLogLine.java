package de.fu_berlin.inf.dpp.ui.widgets.activitylog.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.awareness.actions.ActionType;
import de.fu_berlin.inf.dpp.awareness.actions.ActionTypeDataHolder;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

/**
 * This composite is used to display a line in the activity log tab.
 */
public class ActivityLogLine extends Composite {

    /*
     * Images for different activity types
     */
    public static final Image DIALOG_IMAGE = ImageManager
        .getImage("icons/view16/activitylog/activitylog_dialog.gif");

    public static final Image FILECREATION_IMAGE = ImageManager
        .getImage("icons/view16/activitylog/activitylog_filecreation.gif");

    public static final Image JUNIT_IMAGE = ImageManager
        .getImage("icons/view16/activitylog/activitylog_junit.gif");

    public static final Image REFACTORING_IMAGE = ImageManager
        .getImage("icons/view16/activitylog/activitylog_refactoring.gif");

    public static final Image VIEW_IMAGE = ImageManager
        .getImage("icons/view16/activitylog/activitylog_view.gif");

    // RED
    private static final Color TEST_RUN_RED_COLOR = new Color(null, 0xFF, 0, 0);

    // GREEN
    private static final Color TEST_RUN_GREEN_COLOR = new Color(null, 0, 0xFF,
        0);

    // BLUE
    private static final Color TEST_RUN_BLUE_COLOR = new Color(null, 0, 0, 0xFF);

    private final StyledText text;

    /**
     * Creates a new line in the activity log tab. This line contains an icon,
     * representing the action type, a text and some variable parts of the text
     * (which will be replaced with the information of the <code>data</code> are
     * formatted bold or colored.
     * 
     * @param parent
     *            The parent composite of this composite
     * @param data
     *            The data holder, offering more information about the executed
     *            activity
     * */
    public ActivityLogLine(Composite parent, ActionTypeDataHolder data) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        String message = "  ";

        final int messagePrefixLength = message.length();

        text = new StyledText(this, SWT.WRAP);
        text.setEditable(false);

        int offset = -1;

        if (data.getType() == ActionType.ADD_CREATEDFILE) {

            String fileName = data.getCreatedFileName();

            message += Messages.ActivityLog_tab_line_text_created_file + " "
                + fileName;

            text.setText(message);

            addImage(text, FILECREATION_IMAGE, 0, 1);

            offset = messagePrefixLength
                + Messages.ActivityLog_tab_line_text_created_file.length() + 1;

            printBold(text, offset, fileName.length(), null);

        } else if (data.getType() == ActionType.ADD_IDEELEMENT) {

            String title = data.getIdeTitle();

            if (data.getIdeElementType() == Element.DIALOG) {

                message += Messages.ActivityLog_tab_line_text_open_dialog + " "
                    + title;

                text.setText(message);

                addImage(text, DIALOG_IMAGE, 0, 1);

                offset = messagePrefixLength
                    + Messages.ActivityLog_tab_line_text_open_dialog.length()
                    + 1;

                printBold(text, offset, title.length(), null);

            } else {
                message += Messages.ActivityLog_tab_line_text_active_view + " "
                    + title;

                text.setText(message);

                addImage(text, VIEW_IMAGE, 0, 1);

                offset = messagePrefixLength
                    + Messages.ActivityLog_tab_line_text_active_view.length()
                    + 1;

                printBold(text, offset, title.length(), null);

            }
        } else if (data.getType() == ActionType.ADD_REFACTORING) {

            message += data.getRefactoringDescription();

            text.setText(message);

            addImage(text, REFACTORING_IMAGE, 0, 1);

        } else if (data.getType() == ActionType.ADD_TESTRUN) {

            final State state = data.getTestRunState();
            final String testName = data.getTestRunName();

            if (state == State.UNDEFINED) {
                message += Messages.ActivityLog_tab_line_text_testrun_started
                    + " " + testName;
                offset = messagePrefixLength
                    + Messages.ActivityLog_tab_line_text_testrun_started
                        .length() + 1;
            } else {
                message += Messages.ActivityLog_tab_line_text_testrun_finished
                    + " " + testName + " (" + state + ")";
                offset = messagePrefixLength
                    + Messages.ActivityLog_tab_line_text_testrun_finished
                        .length() + 1;
            }
            text.setText(message);
            addImage(text, JUNIT_IMAGE, 0, 1);
            printBold(text, offset, testName.length(), null);
            offset += testName.length() + 2;

            switch (state) {
            case FAILURE:
                printBold(text, offset, state.toString().length(),
                    TEST_RUN_RED_COLOR);
                break;
            case OK:
                printBold(text, offset, state.toString().length(),
                    TEST_RUN_GREEN_COLOR);
                break;
            case ERROR:
                printBold(text, offset, state.toString().length(),
                    TEST_RUN_BLUE_COLOR);
                break;
            case UNDEFINED:
                break;
            default:
                // NOP
                return;
            }
        }
    }

    /**
     * Formats a given StyledText text to bold. If color is set, the bold
     * printed text will also be colored.
     */
    private void printBold(StyledText text, int startIndex, int length,
        Color color) {
        StyleRange style = new StyleRange();
        style.start = startIndex;
        style.length = length;
        style.fontStyle = SWT.BOLD;

        if (color != null)
            style.foreground = color;

        text.setStyleRange(style);
    }

    private void addImage(final StyledText text, final Image image,
        final int offset, final int length) {
        StyleRange style = new StyleRange();
        style.start = offset;
        style.length = length;
        style.data = image;
        Rectangle rect = image.getBounds();
        style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
        text.setStyleRange(style);

        // For painting the image, a listener must be added. See
        // http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet212.java
        final PaintObjectListener paintListener = new PaintObjectListener() {
            @Override
            public void paintObject(PaintObjectEvent event) {
                StyleRange style = event.style;
                Image image = (Image) style.data;

                if (style.start != offset || image == null
                    || image.isDisposed())
                    return;

                int x = event.x;
                int y = event.y + event.ascent - style.metrics.ascent;
                event.gc.drawImage(image, x, y);
            }
        };

        text.addPaintObjectListener(paintListener);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        text.setBackground(color);
    }

    public String getText() {
        return text.getText();
    }
}