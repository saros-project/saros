/*
 * $Id: DeleteOperation.java 2434 2005-12-12 07:49:51Z sim $
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

/**
 * The DeleteOperation is used to hold a text together with its position that is to be deleted in
 * the document model.
 */
@XStreamAlias("deleteOp")
public class DeleteOperation implements ITextOperation {

  /** The text to be deleted. */
  @XStreamConverter(UrlEncodingStringConverter.class)
  private String replacedText;

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

  /**
   * Instantiates a new delete operation using the given parameters.
   *
   * @param startPosition the start position of the delete operation
   * @param lineDelta how many lines are deleted by the operation
   * @param offsetDelta the offset delta in the last modified line
   * @param replacedText the text removed by this operation
   */
  public DeleteOperation(
      TextPosition startPosition, int lineDelta, int offsetDelta, String replacedText) {
    if (startPosition == null || !startPosition.isValid()) {
      throw new IllegalArgumentException("The given start position must be valid");
    }

    if (lineDelta < 0) {
      throw new IllegalArgumentException("The given line delta must not be negative");
    }
    if (offsetDelta < 0) {
      throw new IllegalArgumentException("The given offset delta must not be negative");
    }

    if (replacedText == null) {
      throw new IllegalArgumentException("The given text must not be null");
    }

    this.startLine = startPosition.getLineNumber();
    this.startInLineOffset = startPosition.getInLineOffset();

    this.lineDelta = lineDelta;
    this.offsetDelta = offsetDelta;

    this.replacedText = replacedText;
  }

  @Override
  public TextPosition getStartPosition() {
    return new TextPosition(startLine, startInLineOffset);
  }

  /**
   * {@inheritDoc}
   *
   * <p>For delete operations, this is the position the deleted text ended before it was deleted.
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
   * Returns the text to be deleted.
   *
   * @return the text to be deleted
   */
  @Override
  public String getText() {
    return this.replacedText;
  }

  @Override
  public String toString() {
    return "Delete(start line: "
        + startLine
        + ", offset: "
        + startInLineOffset
        + ", line delta: "
        + lineDelta
        + ", offset delta: "
        + offsetDelta
        + ", text: '"
        + StringEscapeUtils.escapeJava(StringUtils.abbreviate(replacedText, 150))
        + "')";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteOperation other = (DeleteOperation) obj;

    return this.startLine == other.startLine
        && this.startInLineOffset == other.startInLineOffset
        && this.lineDelta == other.lineDelta
        && this.offsetDelta == other.offsetDelta
        && Objects.equals(this.replacedText, other.replacedText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startLine, startInLineOffset, lineDelta, offsetDelta, replacedText);
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {
    TextPosition startPosition = getStartPosition();

    TextEditActivity textEditActivity =
        new TextEditActivity(
            source, startPosition, 0, 0, "", lineDelta, offsetDelta, replacedText, path);

    return Collections.singletonList(textEditActivity);
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    return Collections.singletonList(this);
  }

  @Override
  public ITextOperation invert() {
    return new InsertOperation(getStartPosition(), lineDelta, offsetDelta, getText());
  }
}
