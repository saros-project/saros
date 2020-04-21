package saros.activities;

import saros.filesystem.IResource;

/** An interface for Activities that are resource-related (e.g. FileActivity) */
public interface IResourceActivity extends IActivity {

  /**
   * Returns the resource that this activity is about.
   *
   * <p>For instance, for creating a file, this resource denotes the file which is created. Must
   * <b>not</b> be <code>null</code>. (Currently can be <code>null</code> for {@link
   * EditorActivity}.)
   *
   * @return the resource that this activity is about
   */
  IResource getResource();
}
