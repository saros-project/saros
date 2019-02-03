package saros.intellij.project;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Wraps the project instance held by the plugin context. This is done to abstract from the specific
 * project object that is used as it can change when the user switches Intellij projects. For
 * convenience' sake, this also wraps the access to the FileEditorManager as it is dependent on the
 * project current object.
 *
 * <p><b>NOTE:</b> To avoid accessing disposed project objects, the project object returned by this
 * class should not be stored in long-term data structures by the caller.
 */
public class ProjectWrapper {

  private Project project;
  private FileEditorManager fileEditorManager;

  /**
   * Instantiates a project wrapper with the given project.
   *
   * @param project the current Project object
   */
  public ProjectWrapper(@NotNull Project project) {
    setProject(project);
  }

  /**
   * Returns the current <code>Project</code> object.
   *
   * @return the current Project object
   */
  @NotNull
  public synchronized Project getProject() {
    return project;
  }

  /**
   * Replaces the held <code>Project</code> object with the passed object. Subsequently updates all
   * held objects dependent on the project.
   *
   * @param project the new <code>Project</code> object
   */
  public synchronized void setProject(@NotNull Project project) {
    this.project = project;

    this.fileEditorManager = FileEditorManager.getInstance(project);
  }

  /**
   * Returns the current <code>FileEditorManager</code> object.
   *
   * @return the current <code>FileEditorManager</code> object
   */
  @NotNull
  public synchronized FileEditorManager getFileEditorManager() {
    return fileEditorManager;
  }
}
