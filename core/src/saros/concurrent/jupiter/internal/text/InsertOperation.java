/*
 * $Id: InsertOperation.java 2755 2006-03-06 09:29:34Z zbinl $
 *
 * ace - a collaborative editor
 * Copyright (C) 2005 Mark Bigler, Simon Raess, Lukas Zbinden
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package saros.concurrent.jupiter.internal.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.editor.text.TextPosition;
import saros.misc.xstream.UrlEncodingStringConverter;
import saros.session.User;
import saros.util.LineSeparatorNormalizationUtil;

/**
 * The InsertOperation is used to hold a text together with its position index. The text is to be
 * inserted in the document model.
 *
 * <p>Insert operations also work with the concept of an original start line and in-line offset.
 * These values describe the position for which the insert operation was originally intended. This
 * concept could be extended in such a way that two origin positions could be compared to each other
 * based on the same context. Therefore, if the two positions do not relate on the same document
 * context, a least synchronization point (LSP) would have to be determined.
 *
 * <p>The text contained in insert operations only uses normalized line separators, meaning it
 * doesn't contain any line separators besides the {@link
 * LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
 *
 * @see LineSeparatorNormalizationUtil
 */
@XStreamAlias("insertOp")
public class InsertOperation implements ITextOperation {

  /** the text to be inserted. */
  @XStreamConverter(UrlEncodingStringConverter.class)
  private String text;

  @XStreamAsAttribute
  @XStreamAlias("sl")
  private final int startLine;

  @XStreamAsAttribute
  @XStreamAlias("so")
  private final int startInLineOffset;

  @XStreamAsAttribute
  @XStreamAlias("ld")
  private final int lineDelta;

  /**
   * The offset delta in the last modified line for the operation.
   *
   * <p>If the operation text does not contain any line breaks (lineDelta=0), this is the relative
   * offset delta to the start of the operation.
   *
   * <p>If the operation text does contain line breaks (lineDelta>0), this is the (absolute) in-line
   * offset in the last line of the operation text.
   */
  @XStreamAsAttribute
  @XStreamAlias("od")
  private final int offsetDelta;

  @XStreamAsAttribute
  @XStreamAlias("ol")
  private final int originStartLine;

  @XStreamAsAttribute
  @XStreamAlias("oo")
  private final int originStartInLineOffset;

  /**
   * Instantiates a new insert operation using the given parameters.
   *
   * <p>Uses the given start position as the origin start position.
   *
   * <p>The given new text must only use normalized line separators, meaning it must not contain any
   * line separators besides the {@link LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @param startPosition the start position of the insert operation
   * @param lineDelta how many lines are added by the operation
   * @param offsetDelta the offset delta in the last modified line
   * @param text the text added by this operation
   * @see #InsertOperation(TextPosition, int, int, String, TextPosition)
   * @see LineSeparatorNormalizationUtil
   */
  public InsertOperation(TextPosition startPosition, int lineDelta, int offsetDelta, String text) {
    this(startPosition, lineDelta, offsetDelta, text, startPosition);
  }

  /**
   * Instantiates a new insert operation using the given parameters.
   *
   * <p>The given new text must only use normalized line separators, meaning it must not contain any
   * line separators besides the {@link LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @param startPosition the start position of the insert operation
   * @param lineDelta how many lines are added by the operation
   * @param offsetDelta the offset delta in the last modified line
   * @param text the text added by this operation
   * @param originStartPosition the original start position the insert operation was meant for; see
   *     {@link #getOriginStartPosition()}
   * @see LineSeparatorNormalizationUtil
   */
  public InsertOperation(
      TextPosition startPosition,
      int lineDelta,
      int offsetDelta,
      String text,
      TextPosition originStartPosition) {

    if (startPosition == null || !startPosition.isValid()) {
      throw new IllegalArgumentException("The given start position must be valid");
    }
    if (originStartPosition == null || !originStartPosition.isValid()) {
      throw new IllegalArgumentException("The given origin start position must be valid");
    }

    if (lineDelta < 0) {
      throw new IllegalArgumentException("The given line delta must not be negative");
    }
    if (offsetDelta < 0) {
      throw new IllegalArgumentException("The given offset delta must not be negative");
    }

    if (text == null) {
      throw new IllegalArgumentException("The given text must not be null");
    }

    this.startLine = startPosition.getLineNumber();
    this.startInLineOffset = startPosition.getInLineOffset();

    this.lineDelta = lineDelta;
    this.offsetDelta = offsetDelta;

    this.originStartLine = originStartPosition.getLineNumber();
    this.originStartInLineOffset = originStartPosition.getInLineOffset();

    this.text = text;
  }

  @Override
  public TextPosition getStartPosition() {
    return new TextPosition(startLine, startInLineOffset);
  }

  /**
   * {@inheritDoc}
   *
   * <p>For insert operations, this is the position where the inserted text ends.
   */
  @Override
  public TextPosition getEndPosition() {
    if (lineDelta == 0) {
      return new TextPosition(startLine, startInLineOffset + offsetDelta);
    } else {
      return new TextPosition(startLine + lineDelta, offsetDelta);
    }
  }

  @Override
  public int getLineDelta() {
    return lineDelta;
  }

  @Override
  public int getOffsetDelta() {
    return offsetDelta;
  }

  /**
   * Returns the position for which the insert operation was originally intended.
   *
   * <p>This concept could be extended in such a way that two origin positions could be compared to
   * each other based on the same context. Therefore, if the two positions do not relate on the same
   * document context, a least synchronization point (LSP) would have to be determined.
   *
   * @return the position for which the insert operation was originally intended
   */
  public TextPosition getOriginStartPosition() {
    return new TextPosition(originStartLine, originStartInLineOffset);
  }

  /**
   * Returns the text to be added by the operation.
   *
   * <p>The returned text only uses normalized line separators, meaning it doesn't contain any line
   * separators besides the {@link LineSeparatorNormalizationUtil#NORMALIZED_LINE_SEPARATOR}.
   *
   * @return the text to be added by the operation
   * @see LineSeparatorNormalizationUtil
   */
  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public String toString() {
    return "Insert(start line: "
        + startLine
        + ", offset: "
        + startInLineOffset
        + ", line delta: "
        + lineDelta
        + ", offset delta: "
        + offsetDelta
        + ", text: '"
        + StringEscapeUtils.escapeJava(StringUtils.abbreviate(text, 150))
        + "', origin start line: "
        + originStartLine
        + ", offset: "
        + originStartInLineOffset
        + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    InsertOperation other = (InsertOperation) obj;

    return this.startLine == other.startLine
        && this.startInLineOffset == other.startInLineOffset
        && this.lineDelta == other.lineDelta
        && this.offsetDelta == other.offsetDelta
        && Objects.equals(this.text, other.text)
        && this.originStartLine == other.originStartLine
        && this.originStartInLineOffset == other.originStartInLineOffset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        startLine,
        startInLineOffset,
        lineDelta,
        originStartInLineOffset,
        text,
        originStartLine,
        originStartInLineOffset);
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {
    TextPosition startPosition = getStartPosition();

    TextEditActivity textEditActivity =
        new TextEditActivity(source, startPosition, lineDelta, offsetDelta, text, 0, 0, "", path);

    return Collections.singletonList(textEditActivity);
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    return Collections.singletonList(this);
  }

  @Override
  public ITextOperation invert() {
    return new DeleteOperation(getStartPosition(), lineDelta, offsetDelta, getText());
  }
}
