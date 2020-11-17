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
import saros.filesystem.IFile;
import saros.session.User;

/**
 * Activity for activating, closing, and saving editors. If the {@link #getResource()} ()} returns
 * <code>null</code> then no resource is currently active.
 *
 * <p>Saving is not document- but editor-specific because one editor might perform changes on the
 * document before actually saving while others just save. An example is a Java editor with save
 * actions enabled vs. a plain text editor for the very same document.
 */
@XStreamAlias("editorActivity")
public class EditorActivity extends AbstractResourceActivity<IFile> {

  public static enum Type {
    ACTIVATED,
    CLOSED,
    SAVED
  }

  @XStreamAsAttribute protected final Type type;

  /**
   * @param file May be <code>null</code> -- only if type is {@link Type#ACTIVATED} -- to denote
   *     that there is no active editor anymore. Must not be <code>null</code> for other types.
   */
  public EditorActivity(User source, Type type, IFile file) {
    super(source, file);

    if (file == null) {
      if (type != Type.ACTIVATED) {
        throw new IllegalArgumentException(
            "Null file for non-activation type EditorActivity given.");
      }
    }

    this.type = type;
  }

  @Override
  public boolean isValid() {
    /*
     * file might be null for Type.ACTIVATED, see ctor and TODO in
     * AbstractResourceActivity#isValid()
     */
    return super.isValid() && (getResource() != null || type == Type.ACTIVATED);
  }

  public Type getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hashCode(type);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof EditorActivity)) return false;

    EditorActivity other = (EditorActivity) obj;

    if (this.type != other.type) return false;

    return true;
  }

  @Override
  public String toString() {
    return "EditorActivity(type: " + type + ", file: " + getResource() + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
