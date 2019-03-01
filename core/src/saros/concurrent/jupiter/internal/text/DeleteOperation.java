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
 * The DeleteOperation is used to hold a text together with its position that is to be deleted in
 * the document model.
 */
@XStreamAlias("deleteOp")
public class DeleteOperation implements ITextOperation {

  /** the text to be deleted. */
  @XStreamConverter(UrlEncodingStringConverter.class)
  private String text;

  /** the position in the document where the text is to be deleted. */
  @XStreamAsAttribute private int position;

  /** @param text the text to be deleted */
  public DeleteOperation(int position, String text) {
    if (position < 0) {
      throw new IllegalArgumentException("position index must be >= 0");
    }
    this.position = position;

    if (text == null) {
      throw new IllegalArgumentException("text may not be null");
    }
    this.text = text;
  }

  @Override
  public int getPosition() {
    return this.position;
  }

  @Override
  public int getTextLength() {
    return this.text.length();
  }

  /**
   * Returns the text to be deleted.
   *
   * @return the text to be deleted
   */
  @Override
  public String getText() {
    return this.text;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Delete("
        + this.position
        + ",'"
        + StringEscapeUtils.escapeJava(StringUtils.abbreviate(this.text, 150))
        + "')";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeleteOperation other = (DeleteOperation) obj;
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
    result = prime * result + position;
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  @Override
  public List<TextEditActivity> toTextEdit(SPath path, User source) {
    return Collections.singletonList(
        new TextEditActivity(source, getPosition(), "", getText(), path));
  }

  @Override
  public List<ITextOperation> getTextOperations() {
    return Collections.singletonList((ITextOperation) this);
  }

  /** {@inheritDoc} */
  @Override
  public ITextOperation invert() {
    return new InsertOperation(getPosition(), getText());
  }
}
