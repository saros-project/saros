package saros.intellij.eventhandler.colorscheme;

import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.IProjectEventHandler;

/**
 * Reloads all current annotations when the IDE color scheme changes to ensure that the annotation
 * colors match the new theme.
 */
public class AnnotationReloader implements IProjectEventHandler {
  private final Project project;
  private final AnnotationManager annotationManager;

  private MessageBusConnection messageBusConnection;
  private boolean enabled;
  private boolean disposed;

  private final EditorColorsListener editorColorsListener = scheme -> reloadAnnotations();

  public AnnotationReloader(Project project, AnnotationManager annotationManager) {
    this.project = project;
    this.annotationManager = annotationManager;

    this.enabled = false;
    this.disposed = false;
  }

  /**
   * Reloads all current Saros annotations.
   *
   * @see AnnotationManager#reloadAnnotations()
   */
  private void reloadAnnotations() {
    annotationManager.reloadAnnotations();
  }

  @NotNull
  @Override
  public ProjectEventHandlerType getHandlerType() {
    return ProjectEventHandlerType.COLOR_SCHEME_CHANGE_HANDLER;
  }

  @Override
  public void initialize() {
    setEnabled(true);
  }

  @Override
  public void dispose() {
    disposed = true;
    setEnabled(false);
  }

  @Override
  public void setEnabled(boolean enabled) {
    assert !disposed || !enabled : "disposed listeners must not be enabled";

    if (!this.enabled && enabled) {
      messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(EditorColorsManager.TOPIC, editorColorsListener);

      this.enabled = true;

    } else if (this.enabled && !enabled) {
      messageBusConnection.disconnect();
      messageBusConnection = null;

      this.enabled = false;
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
