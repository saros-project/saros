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
package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("textSelectionActivity")
public class TextSelectionActivity extends AbstractResourceActivity {

  @XStreamAlias("o")
  @XStreamAsAttribute
  protected final int offset;

  @XStreamAlias("l")
  @XStreamAsAttribute
  protected final int length;

  public TextSelectionActivity(User source, int offset, int length, SPath path) {
    super(source, path);

    if (path == null) throw new IllegalArgumentException("path must not be null");

    this.offset = offset;
    this.length = length;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getPath() != null);
  }

  public int getLength() {
    return this.length;
  }

  public int getOffset() {
    return this.offset;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + length;
    result = prime * result + offset;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof TextSelectionActivity)) return false;

    TextSelectionActivity other = (TextSelectionActivity) obj;

    if (this.offset != other.offset) return false;
    if (this.length != other.length) return false;

    return true;
  }

  @Override
  public String toString() {
    return "TextSelectionActivity(offset: "
        + offset
        + ", length: "
        + length
        + ", src: "
        + getSource()
        + ", path: "
        + getPath()
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
