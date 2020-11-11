package saros.intellij.editor;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.intellij.filesystem.IntellijReferencePoint;

/**
 * Editor pool for background editors. The pool uses an LRU cache to avoid having to many background
 * editors open. The capacity is defined in {@link #CAPACITY}.
 *
 * <p>All methods of this class are synchronized to ensure that all editor references are
 * instantiated, handled, and disposed correctly.
 *
 * <p>The editor pool is cleared every time a session ends. Furthermore, it is cleared before the
 * application is torn down. This is necessary as an abrupt application end can lead to the plugin
 * being torn down before the current session is correctly terminated. Such a termination would
 * otherwise lead to dangling background editor references on teardown.
 *
 * <p><b>NOTE:</b> This class is only meant to allow optimized access to the editor API for
 * resources that are currently not open in an editor. Editors obtained through this method are not
 * actually visible to the user.
 *
 * <p>To open user-visible editors, use {@link ProjectAPI#openEditor(Project, VirtualFile, boolean)}
 * instead.
 */
public class BackgroundEditorPool implements Disposable {

  private static final Logger log = Logger.getLogger(BackgroundEditorPool.class);

  /** The capacity for the LRU cache. */
  private static final int CAPACITY = 20;

  private final Map<IFile, Editor> backgroundEditors;

  private final MessageBusConnection messageBusConnection;

  /**
   * Resets the background editor pool before the application is closed.
   *
   * <p>This explicit handling is necessary as the teardown as part of {@link Disposable#dispose()}
   * registered to the {@link Application} acts to late, leading to issues where the IDE detects the
   * remaining editor references as memory leaks as they are only released after the teardown check.
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final AppLifecycleListener appLifecycleListener =
      new AppLifecycleListener() {
        @Override
        public void appWillBeClosed(boolean isRestart) {
          if (!backgroundEditors.isEmpty()) {
            clear();
          }
        }
      };

  /**
   * Releases and drops entries belonging to the closed project from the background editor pool
   * before the project is closed.
   *
   * <p>This explicit handling is necessary as the teardown as part of {@link
   * com.intellij.openapi.project.ProjectCloseHandler} acts to late (as leaving the session is run
   * on a separate thread), leading to issues where the IDE detects the remaining editor references
   * as memory leaks as they are only released after the teardown check.
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final ProjectManagerListener projectLifecycleListener =
      new ProjectManagerListener() {
        @Override
        public void projectClosingBeforeSave(@NotNull Project project) {
          dropEditorsForProject(project);
        }
      };

  /**
   * Instantiates a new background editor pool.
   *
   * <p>Registers the pool to be torn down on application teardown to correctly release all held
   * editor references.
   */
  BackgroundEditorPool() {
    backgroundEditors = new LRUCache(CAPACITY);

    Application application = ApplicationManager.getApplication();

    this.messageBusConnection = application.getMessageBus().connect();
    messageBusConnection.subscribe(AppLifecycleListener.TOPIC, appLifecycleListener);
    messageBusConnection.subscribe(ProjectManager.TOPIC, projectLifecycleListener);

    Disposer.register(application, this);
  }

  /**
   * Returns a background editor for the given document. If no such editor is present in the
   * mapping, a new one will be created and added to the mapping.
   *
   * <p><b>NOTE:</b> This method is only meant to allow optimized access to the editor API for
   * resources that are currently not open in an editor. Editors obtained through this method are
   * not actually visible to the user.
   *
   * <p>To open user-visible editors, use {@link ProjectAPI#openEditor(Project, VirtualFile,
   * boolean)} instead.
   *
   * @param file the file for the document
   * @param document the document to open a background editor for.
   * @return a background editor for hte given document
   * @see ProjectAPI#createBackgroundEditor(Document, Project)
   */
  @NotNull
  synchronized Editor getBackgroundEditor(@NotNull IFile file, @NotNull Document document) {
    if (backgroundEditors.containsKey(file)) {
      return backgroundEditors.get(file);
    }

    Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

    Editor backgroundEditor = ProjectAPI.createBackgroundEditor(document, project);
    log.debug("Created background editor for " + backgroundEditor.getDocument());

    backgroundEditors.put(file, backgroundEditor);

    return backgroundEditor;
  }

  /**
   * Removes the background editor from the mapping and releases the reference.
   *
   * @param file the file whose background editor to release
   */
  synchronized void dropBackgroundEditor(@NotNull IFile file) {
    if (!backgroundEditors.containsKey(file)) {
      return;
    }

    Editor backgroundEditor = backgroundEditors.remove(file);

    releaseBackgroundEditor(backgroundEditor);
  }

  /** Releases all held background editors and then drops the mapping. */
  synchronized void clear() {
    log.debug("Cleaning up background editor pool");

    for (Editor backgroundEditor : backgroundEditors.values()) {
      releaseBackgroundEditor(backgroundEditor);
    }

    backgroundEditors.clear();
  }

  /**
   * Releases all editors for the given project and removes them from the pool.
   *
   * @param project the project whose editors to release amd drop
   */
  private synchronized void dropEditorsForProject(@NotNull Project project) {
    Iterator<Entry<IFile, Editor>> it = backgroundEditors.entrySet().iterator();

    while (it.hasNext()) {
      Entry<IFile, Editor> entry = it.next();
      Editor editor = entry.getValue();

      if (project.equals(editor.getProject())) {
        it.remove();

        releaseBackgroundEditor(editor);
      }
    }
  }

  /**
   * Releases the given background editor.
   *
   * @param backgroundEditor the background editor to release
   * @see ProjectAPI#releaseBackgroundEditor(Editor)
   */
  private static void releaseBackgroundEditor(@NotNull Editor backgroundEditor) {
    log.debug("Dropping and releasing background editor for " + backgroundEditor.getDocument());
    ProjectAPI.releaseBackgroundEditor(backgroundEditor);
  }

  /**
   * Returns whether the background editor pool is empty.
   *
   * @return whether the background editor pool is empty
   */
  synchronized boolean isEmpty() {
    return backgroundEditors.isEmpty();
  }

  @Override
  public synchronized void dispose() {
    messageBusConnection.disconnect();
  }

  /**
   * Implementation of a least recently used cache using {@link LinkedHashMap}.
   *
   * <p>Uses the default load factor {@link #LOAD_FACTOR}.
   *
   * @see LinkedHashMap
   * @see LinkedHashMap#removeEldestEntry(Map.Entry)
   */
  @SuppressWarnings("serial")
  private static class LRUCache extends LinkedHashMap<IFile, Editor> {
    /** The load factor for the LRU cache. */
    private static final float LOAD_FACTOR = 0.75f;

    private final int capacity;

    /**
     * Instantiates a new LRU cache with the given capacity.
     *
     * @param capacity the capacity the cache has
     * @see LinkedHashMap#LinkedHashMap(int, float, boolean)
     */
    LRUCache(int capacity) {
      super(calculateCapacity(capacity), LOAD_FACTOR, true);

      this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Entry<IFile, Editor> eldest) {
      if (size() >= capacity) {
        Editor editor = eldest.getValue();

        releaseBackgroundEditor(editor);

        IFile file = eldest.getKey();
        Document document = editor.getDocument();
        log.debug("Evicting least recently used entry from cache: " + file + " - " + document);

        return true;
      }

      return false;
    }

    /**
     * Calculates an initial capacity that ensures the hash map will never have to be resized for
     * the given actual capacity and load factor.
     *
     * @param actualCapacity the actual capacity the cache is supposed to have
     * @return the initial capacity to use
     */
    private static int calculateCapacity(int actualCapacity) {
      return (int) Math.ceil(actualCapacity / LOAD_FACTOR);
    }
  }
}
