package de.fu_berlin.inf.dpp.ui.widgets.chat.parts;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatDisplayListener;

/**
 * This composite displays a chat conversation between users.
 */
public class IRCStyleChatDisplay extends Composite {

    private final DateFormat dateFormatter = DateFormat
        .getTimeInstance(DateFormat.SHORT);

    private final List<IChatDisplayListener> chatDisplayListeners = new ArrayList<IChatDisplayListener>();

    private final StyledText display;

    private boolean appendLineBreak = false;

    private int decorationOffsetStart = 0;

    private static class WordLocation {

        public final int offset;
        public final int length;

        public WordLocation(final int offset, final int length) {
            this.offset = offset;
            this.length = length;
        }
    }

    public IRCStyleChatDisplay(final Composite parent, final int style) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        display = new StyledText(this, SWT.FULL_SELECTION | SWT.MULTI
            | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | (style & SWT.BORDER));

        addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                display.setFocus();
            }
        });

        addHyperLinkListener(display);
    }

    /**
     * Displays a new line containing the supplied message and a separator line
     * if necessary.
     * 
     * @param jid
     *            JID of the sender who composed the message
     * @param color
     *            the color to be used to mark the user
     * @param message
     *            composed by the sender
     * @param receivedOn
     *            the date the message was received
     */
    public void addChatLine(final JID jid, final String displayName,
        final Color color, final String message, final Date receivedOn) {

        if (!appendLineBreak)
            appendLineBreak = true;
        else
            display.append("\n");

        display.append("[" + dateFormatter.format(receivedOn) + "] ");
        display.append("<" + displayName + "> " + message);
        decorateContent();

        display.setTopIndex(display.getLineCount() - 1);
    }

    /**
     * Updates the color of the chat line separators for a specific JID.
     * 
     * @param jid
     *            JID whose color should be updated
     * @param color
     *            the new color
     */
    public void updateColor(JID jid, Color color) {

        // for (Control control : contentComposite.getChildren()) {
        // if (!jid.equals(control.getData()))
        // continue;
        //
        // if (control instanceof ChatLineSeparator
        // || control instanceof ChatLinePartnerChangeSeparator)
        // control.setBackground(color);
        // }
    }

    /**
     * Updates the display name of the chat line separators for a specific JID.
     * 
     * @param jid
     *            the JID whose display name should be updated
     * @param displayName
     *            the new display name
     */
    public void updateDisplayName(JID jid, String displayName) {
        // for (Control control : contentComposite.getChildren()) {
        // if (!jid.equals(control.getData()))
        // continue;
        //
        // if (control instanceof ChatLinePartnerChangeSeparator) {
        // ChatLinePartnerChangeSeparator separator =
        // (ChatLinePartnerChangeSeparator) control;
        // separator.setUsername(displayName);
        // }
        // }
    }

    /**
     * Adds a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void addChatDisplayListener(final IChatDisplayListener chatListener) {
        chatDisplayListeners.add(chatListener);
    }

    /**
     * Removes a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void removeChatListener(final IChatDisplayListener chatListener) {
        chatDisplayListeners.remove(chatListener);
    }

    // TODO duplicate code, see ChatLine class
    private void addHyperLinkListener(final StyledText text) {
        text.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {

                    int offset = text.getOffsetAtLocation(new Point(event.x,
                        event.y));

                    StyleRange style = text.getStyleRangeAtOffset(offset);
                    if (style != null && style.underline
                        && style.underlineStyle == SWT.UNDERLINE_LINK) {
                        String url = (String) style.data;
                        SWTUtils.openInternalBrowser(url, url);
                    }
                } catch (IllegalArgumentException e) {
                    // no character under event.x, event.y
                }
            }
        });
    }

    /**
     * Decorates the content of the chat display. This method keeps tracks of
     * the already decorated content and will only decorate content that was not
     * decorated before, i.e text that was appended using
     * {@link StyledText#append(String)}
     */
    private void decorateContent() {

        final int contentLenght = display.getCharCount();

        if (decorationOffsetStart >= contentLenght) {
            decorationOffsetStart = contentLenght;
            return;
        }

        final String content = display.getText(decorationOffsetStart,
            contentLenght - 1);

        final List<WordLocation> wordLocations = getWordLocations(content);

        for (final Iterator<WordLocation> it = wordLocations.iterator(); it
            .hasNext();) {

            WordLocation wordLocation = it.next();

            String word = content.substring(wordLocation.offset,
                wordLocation.offset + wordLocation.length);

            if (!isValidURL(word))
                it.remove();
        }

        if (wordLocations.isEmpty())
            return;

        final StyleRange[] styles = new StyleRange[wordLocations.size()];

        final int[] ranges = new int[wordLocations.size() * 2];

        int rangeIdx = 0, styleIdx = 0;

        for (final WordLocation wordLocation : wordLocations) {
            ranges[rangeIdx++] = wordLocation.offset + decorationOffsetStart;
            ranges[rangeIdx++] = wordLocation.length;

            StyleRange style = new StyleRange();
            style.underline = true;
            style.underlineStyle = SWT.UNDERLINE_LINK;
            style.data = content.substring(wordLocation.offset,
                wordLocation.offset + wordLocation.length);
            styles[styleIdx++] = style;
        }

        if (styles.length > 0) {
            display.setStyleRanges(decorationOffsetStart, contentLenght
                - decorationOffsetStart, ranges, styles);
        }

        decorationOffsetStart = contentLenght;
    }

    // TODO duplicate code, see ChatLine class
    /**
     * Returns a list of {@linkplain WordLocation word locations} for the given
     * text. Each word location will contain the offset (0 based) indicating the
     * start inside the text and its length. A word is considered to not contain
     * any whitespace characters and its length is greater or equal to 1. This
     * method <b>offers no support</b> for escaping whitespace characters.
     * 
     * <pre>
     * Example: " foo bar" (without quotes) will return the word locations
     * 1, 3
     * 5, 3
     * </pre>
     * 
     * 
     * @param text
     *            the text to be searched
     * @return list of {@linkplain WordLocation word locations} containing the
     *         offset and length of each word
     * @see Character#isWhitespace(char)
     */
    private static List<WordLocation> getWordLocations(final String text) {

        final List<WordLocation> locations = new ArrayList<WordLocation>();

        int startIdx = 0, currentIdx = 0;

        for (final Character c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {

                if (currentIdx > startIdx)
                    locations.add(new WordLocation(startIdx, currentIdx
                        - startIdx));

                startIdx = currentIdx + 1;
            }

            currentIdx++;
        }

        if (currentIdx > startIdx)
            locations.add(new WordLocation(startIdx, currentIdx - startIdx));

        return locations;
    }

    // TODO duplicate code, see ChatLine class
    private boolean isValidURL(final String urlString) {
        try {
            final URL url = new URL(urlString);
            /*
             * spec does not say anything about null but it seems some JVM
             * distr. return null
             */
            final String host = url.getHost();
            return host != null && !host.isEmpty();
        } catch (Exception e) {
            /*
             * ignore just return false as a non decorated message is still
             * better than no message at all
             */
            return false;
        }
    }
}
