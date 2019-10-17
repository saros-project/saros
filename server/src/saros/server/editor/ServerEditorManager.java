package saros.server.editor;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.server.filesystem.ServerFileImpl;
import saros.server.filesystem.ServerReferencePointManager;
import saros.server.filesystem.ServerWorkspaceImpl;
import saros.session.User;

/** Server implementation of the {@link IEditorManager} interface */
public class ServerEditorManager implements IEditorManager {

  private static final Logger LOG = Logger.getLogger(ServerEditorManager.class);

  private Map<SPath, Editor> openEditors = Collections.synchronizedMap(new LRUMap<>(10));
  private List<ISharedEditorListener> listeners = new CopyOnWriteArrayList<>();

  private final ServerReferencePointManager serverReferencePointManager;
  private final IWorkspace workspace;

  public ServerEditorManager(
      ServerReferencePointManager serverReferencePointManager, ServerWorkspaceImpl workspace) {
    this.serverReferencePointManager = serverReferencePointManager;
    this.workspace = workspace;
  }

  @Override
  public void openEditor(SPath path, boolean activate) {
    try {
      getOrCreateEditor(path);
    } catch (IOException e) {
      LOG.warn("Could not open editor for " + path);
    }
  }

  @Override
  public Set<SPath> getOpenEditors() {
    return openEditors.keySet();
  }

  @Override
  public String getContent(SPath path) {
    try {
      return getOrCreateEditor(path).getContent();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void saveEditors(IReferencePoint referencePoint) {
    // do nothing?
    // we do not keep dirty editors,
    // because the LRUMap might close Editors at any time
  }

  @Override
  public void adjustViewport(SPath path, LineRange range, TextSelection selection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void jumpToUser(User target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSharedEditorListener(ISharedEditorListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeSharedEditorListener(ISharedEditorListener listener) {
    listeners.remove(listener);
  }

  /**
   * Get an existing or create a new Editor for a given path. May remove the least recently used
   * Editor to free memory.
   *
   * @param path of the file to open
   * @return Editor of the file
   * @throws IOException
   */
  private Editor getOrCreateEditor(SPath path) throws IOException {
    Editor editor = openEditors.get(path);
    if (editor == null) {
      Path javaPath = Paths.get(path.getFullPath().toString());
      File javaFile = javaPath.toFile();

      if (javaFile == null) {
        throw new NoSuchFileException(path.toString());
      }

      if (!javaFile.isFile()) {
        throw new IOException("Not a file: " + path);
      }

      IFile file = new ServerFileImpl(workspace, path.getProjectRelativePath());

      editor = new Editor(file);
      openEditors.put(path, editor);
    }
    return editor;
  }

  /**
   * Executes a text edit activity on the matching editor.
   *
   * @param activity the activity describing the text edit to apply
   */
  public void applyTextEdit(TextEditActivity activity) {
    SPath path = activity.getPath();
    try {
      Editor editor = getOrCreateEditor(path);
      editor.applyTextEdit(activity);
      editor.save();
      for (ISharedEditorListener listener : listeners) {
        listener.textEdited(activity);
      }
    } catch (IOException e) {
      LOG.error("Could not read " + path + " to apply text edit", e);
    }
  }

  /**
   * Updates the mapping of an open editor to a new file path
   *
   * @param oldPath the old file path
   * @param newPath the new file path
   */
  public void updateMapping(SPath oldPath, SPath newPath) {
    Editor oldEditor = openEditors.remove(oldPath);
    openEditors.put(newPath, oldEditor);
  }

  @Override
  public void closeEditor(SPath path) {
    openEditors.remove(path);
  }

  /**
   * Close all editors of files in a specific folder. Helpful if a folder gets deleted.
   *
   * @param folder path of the folder
   */
  public void closeEditorsInFolder(SPath folder) {
    synchronized (openEditors) {
      Set<SPath> keys = openEditors.keySet();
      Set<SPath> invalidKeys = new HashSet<>();
      for (SPath path : keys) {
        if (folder.getFullPath().isPrefixOf(path.getFullPath())) {
          invalidKeys.add(path);
        }
      }
      for (SPath path : invalidKeys) {
        closeEditor(path);
      }
    }
  }
}
