package saros.activities;

/** An interface for Activities that are resource-related (e.g. FileActivity) */
public interface IResourceActivity extends IActivity {

  /**
   * @return the path of the file that this Activity is about. For instance for creating a file this
   *     path denotes the file which is created. Must <b>not</b> be <code>null</code>.
   */
  public SPath getPath();
}
