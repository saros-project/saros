package saros.ui.widgets.chat.items;

import java.net.URL;
import java.util.ArrayList;
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
import saros.ui.util.SWTUtils;

/**
 * This composite is used to display a chat message.
 *
 * @author bkahlert
 */
public class ChatLine extends Composite {
  protected StyledText text;

  private static class WordLocation {
    public int offset;
    public int length;

    public WordLocation(int offset, int length) {
      this.offset = offset;
      this.length = length;
    }
  }

  public ChatLine(Composite parent, String message) {
    super(parent, SWT.NONE);
    this.setLayout(new FillLayout());

    text = new StyledText(this, SWT.WRAP);
    text.setText(message);
    text.setEditable(false);
    decorateHyperLinks(text);
    addHyperLinkListener(text);
  }

  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    text.setBackground(color);
  }

  public String getText() {
    return text.getText();
  }

  /**
   * Returns a list of {@linkplain WordLocation word locations} for the given text. Each word
   * location will contain the offset (0 based) indicating the start inside the text and its length.
   * A word is considered to not contain any whitespace characters and its length is greater or
   * equal to 1. This method <b>offers no support</b> for escaping whitespace characters.
   *
   * <pre>
   * Example: " foo bar" (without quotes) will return the word locations
   * 1, 3
   * 5, 3
   * </pre>
   *
   * @param text the text to be searched
   * @return list of {@linkplain WordLocation word locations} containing the offset and length of
   *     each word
   */
  private List<WordLocation> getWordLocations(String text) {

    List<WordLocation> links = new ArrayList<WordLocation>();

    int startIdx = 0, currentIdx = 0;

    for (Character c : text.toCharArray()) {
      if (Character.isWhitespace(c)) {

        if (currentIdx > startIdx) links.add(new WordLocation(startIdx, currentIdx - startIdx));

        startIdx = currentIdx + 1;
      }

      currentIdx++;
    }

    if (currentIdx > startIdx) links.add(new WordLocation(startIdx, currentIdx - startIdx));

    return links;
  }

  private void decorateHyperLinks(StyledText text) {

    String content = text.getText();

    List<WordLocation> wordLocations = getWordLocations(content);

    for (Iterator<WordLocation> it = wordLocations.iterator(); it.hasNext(); ) {

      WordLocation wordLocation = it.next();

      String word =
          content.substring(wordLocation.offset, wordLocation.offset + wordLocation.length);

      if (!isValidURL(word)) it.remove();
    }

    if (wordLocations.isEmpty()) return;

    StyleRange[] styles = new StyleRange[wordLocations.size()];

    int[] ranges = new int[wordLocations.size() * 2];

    int rangeIdx = 0, styleIdx = 0;

    for (WordLocation wordLocation : wordLocations) {
      ranges[rangeIdx++] = wordLocation.offset;
      ranges[rangeIdx++] = wordLocation.length;

      StyleRange style = new StyleRange();
      style.underline = true;
      style.underlineStyle = SWT.UNDERLINE_LINK;
      style.data =
          content.substring(wordLocation.offset, wordLocation.offset + wordLocation.length);
      styles[styleIdx++] = style;
    }

    text.setStyleRanges(ranges, styles);
  }

  private void addHyperLinkListener(final StyledText text) {
    text.addListener(
        SWT.MouseDown,
        new Listener() {
          @Override
          public void handleEvent(Event event) {
            try {

              int offset = text.getOffsetAtLocation(new Point(event.x, event.y));

              StyleRange style = text.getStyleRangeAtOffset(offset);
              if (style != null && style.underline && style.underlineStyle == SWT.UNDERLINE_LINK) {
                String url = (String) style.data;
                SWTUtils.openInternalBrowser(url, url);
              }
            } catch (IllegalArgumentException e) {
              // no character under event.x, event.y
            }
          }
        });
  }

  private boolean isValidURL(String urlString) {
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
