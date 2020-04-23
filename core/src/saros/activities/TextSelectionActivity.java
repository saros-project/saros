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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Objects;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.session.User;

@XStreamAlias("textSelectionActivity")
public class TextSelectionActivity extends AbstractResourceActivity<IFile> {

  @XStreamAlias("sl")
  @XStreamAsAttribute
  private final int startLine;

  @XStreamAlias("so")
  @XStreamAsAttribute
  private final int startInLineOffset;

  @XStreamAlias("el")
  @XStreamAsAttribute
  private final int endLine;

  @XStreamAlias("eo")
  @XStreamAsAttribute
  private final int endInLineOffset;

  /**
   * Instantiates a new text selection activity.
   *
   * @param source the user that created the selection
   * @param selection the text selection
   * @param file the file the text was selected in
   * @throws IllegalArgumentException if the source or file is <code>null</code>
   */
  public TextSelectionActivity(User source, TextSelection selection, IFile file) {
    super(source, file);

    if (file == null) throw new IllegalArgumentException("file must not be null");

    TextPosition startPosition = selection.getStartPosition();
    this.startLine = startPosition.getLineNumber();
    this.startInLineOffset = startPosition.getInLineOffset();

    TextPosition endPosition = selection.getEndPosition();
    this.endLine = endPosition.getLineNumber();
    this.endInLineOffset = endPosition.getInLineOffset();
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getResource() != null);
  }

  /**
   * Returns the text selection contained in this activity.
   *
   * @return the text selection contained in the activity
   */
  public TextSelection getSelection() {
    TextPosition startPosition = new TextPosition(startLine, startInLineOffset);
    TextPosition endPosition = new TextPosition(endLine, endInLineOffset);

    return new TextSelection(startPosition, endPosition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), startLine, startInLineOffset, endLine, endInLineOffset);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof TextSelectionActivity)) return false;

    TextSelectionActivity other = (TextSelectionActivity) obj;

    return this.startLine == other.startLine
        && this.startInLineOffset == other.startInLineOffset
        && this.endLine == other.endLine
        && this.endInLineOffset == other.endInLineOffset;
  }

  @Override
  public String toString() {
    return "TextSelectionActivity(start line: "
        + startLine
        + ", in-line offset: "
        + startInLineOffset
        + ", end line: "
        + endLine
        + ", in-line offset: "
        + endInLineOffset
        + ", src: "
        + getSource()
        + ", file: "
        + getResource()
        + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    /**
     * @JTourBusStop 13, Activity sending, Third dispatch:
     *
     * <p>Each specific activity implementation does the same simple third dispatch: It uses the
     * given receiver to deliver itself to it, so the IActivityReceiver implementation gets a
     * correctly typed activity without having to use "instanceof" constructions.
     */
    receiver.receive(this);
  }
}
