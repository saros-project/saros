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

package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.misc.xstream.UrlEncodingStringConverter;
import de.fu_berlin.inf.dpp.session.User;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The InsertOperation is used to hold a text together with its position index. The text is to be
 * inserted in the document model.
 */
@XStreamAlias("insertOp")
public class InsertOperation implements ITextOperation {

  /** the text to be inserted. */
  @XStreamConverter(UrlEncodingStringConverter.class)
  private String text;

  /** the position index in the document */
  @XStreamAsAttribute private int position;

  /**
   * the origin position index where the insert operation was originally intended. This concept
   * could be extended in such a way that two origin positions could be compared to each other based
   * on the same context. Therefore, if the two positions do not relate on the same document
   * context, a least synchronization point (LSP) would have to be determined.
   */
  @XStreamAsAttribute private int origin;

  /**
   * Syntactic sugar for creating a new InsertOperation with the origin set to the given position.
   *
   * @param position the position in the document
   * @param text the text to be inserted
   */
  public InsertOperation(int position, String text) {
    this(position, text, position);
  }

  /**
   * @param position the position in the document
   * @param text the text to be inserted
   * @param origin the origin position of this insert operation
   */
  public InsertOperation(int position, String text, int origin) {
    if (position < 0) {
      throw new IllegalArgumentException("position index must be >= 0");
    }
    this.position = position;
    if (text == null) {
      throw new IllegalArgumentException("text may not be null");
    }
    this.text = text;
    if (origin < 0) {
      throw new IllegalArgumentException("origin index must be >= 0");
    }
    this.origin = origin;
  }

  /**
   * @param position the position in the document
   * @param text the text to be inserted
   * @param origin the origin position of this insert operation
   * @param isUndo flag to indicate an undo operation
   */
  public InsertOperation(int position, String text, int origin, boolean isUndo) {
    this(position, text, origin);
  }

  @Override
  public int getPosition() {
    return this.position;
  }

  /** @return the text to be inserted */
  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public int getTextLength() {
    return this.text.length();
  }

  /** @return the origin position */
  public int getOrigin() {
    return this.origin;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Insert("
        + this.position
        + ",'"
        + StringEscapeUtils.escapeJava(StringUtils.abbreviate(this.text, 150))
        + "',"
        + this.origin
        + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    InsertOperation other = (InsertOperation) obj;
    if (origin != other.origin) return false;
    if (position != other.position) return false;
    if (text == null) {
      if (other.text != null) return false;
    } else if (!text.equals(other.text)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + origin;
    result = prime * result + position;
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {
    return Collections.singletonList(
        new TextEditActivity(source, getPosition(), getText(), "", path));
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    return Collections.singletonList((ITextOperation) this);
  }

  /** {@inheritDoc} */
  @Override
  public ITextOperation invert() {
    return new DeleteOperation(getPosition(), getText());
  }
}
