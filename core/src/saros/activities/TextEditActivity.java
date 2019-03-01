/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package saros.activities;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.concurrent.jupiter.internal.text.SplitOperation;
import saros.session.User;

/**
 * An immutable TextEditActivity.
 *
 * @author rdjemili
 */
public class TextEditActivity extends AbstractResourceActivity {

  private static final Logger log = Logger.getLogger(TextEditActivity.class);

  protected final int offset;
  protected final String text;
  protected final String replacedText;

  /**
   * @param offset the offset inside the document where this Activity happened.
   * @param text the text that was inserted.
   * @param replacedText the text that was replaced by this Activity.
   * @param path path of the editor where this Activity happened.
   * @param source JID of the user that caused this Activity
   */
  public TextEditActivity(User source, int offset, String text, String replacedText, SPath path) {

    super(source, path);

    if (text == null) throw new IllegalArgumentException("Text cannot be null");
    if (replacedText == null) throw new IllegalArgumentException("ReplacedText cannot be null");
    if (path == null) throw new IllegalArgumentException("Editor cannot be null");

    this.offset = offset;
    this.text = text;
    this.replacedText = replacedText;
  }

  public int getOffset() {
    return offset;
  }

  public String getText() {
    return text;
  }

  public String getReplacedText() {
    return replacedText;
  }

  @Override
  public String toString() {
    String newText = StringEscapeUtils.escapeJava(StringUtils.abbreviate(text, 150));
    String oldText = StringEscapeUtils.escapeJava(StringUtils.abbreviate(replacedText, 150));
    return "TextEditActivity(offset: "
        + offset
        + ", new: '"
        + newText
        + "', old: '"
        + oldText
        + "', path: "
        + getPath()
        + ", src: "
        + getSource()
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + offset;
    result = prime * result + ObjectUtils.hashCode(replacedText);
    result = prime * result + ObjectUtils.hashCode(text);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof TextEditActivity)) return false;

    TextEditActivity other = (TextEditActivity) obj;

    if (this.offset != other.offset) return false;
    if (!ObjectUtils.equals(this.replacedText, other.replacedText)) return false;
    if (!ObjectUtils.equals(this.text, other.text)) return false;

    return true;
  }

  /**
   * Compare text edit information without source settings.
   *
   * @param obj TextEditActivity Object
   * @return true if edit information equals. false otherwise.
   */
  public boolean sameLike(Object obj) {
    if (obj instanceof TextEditActivity) {
      TextEditActivity other = (TextEditActivity) obj;
      return (this.offset == other.offset)
          && (this.getPath() != null)
          && (other.getPath() != null)
          && this.getPath().equals(other.getPath())
          && this.text.equals(other.text)
          && (this.replacedText.equals(other.replacedText));
    }
    return false;
  }

  /** Convert this TextEditActivity to an Operation */
  public Operation toOperation() {

    // delete Activity
    if ((replacedText.length() > 0) && (text.length() == 0)) {
      return new DeleteOperation(offset, replacedText);
    }

    // insert Activity
    if ((replacedText.length() == 0) && (text.length() > 0)) {
      return new InsertOperation(offset, text);
    }

    // replace operation has to be split into delete and insert operation
    if ((replacedText.length() > 0) && (text.length() > 0)) {
      return new SplitOperation(
          new DeleteOperation(offset, replacedText), new InsertOperation(offset, text));
    }

    // Cannot happen
    // assert false; seems it can

    log.warn("NoOp Text edit: new '" + text + "' old '" + replacedText + "'");
    return new NoOperation();
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
