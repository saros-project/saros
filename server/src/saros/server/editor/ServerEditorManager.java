package saros.server.editor;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextPositionUtils;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.server.filesystem.ServerFileImpl;
import saros.server.filesystem.ServerFolderImpl;
import saros.session.User;
import saros.util.LineSeparatorNormalizationUtil;

/** Server implementation of the {@link IEditorManager} interface */
public class ServerEditorManager implements IEditorManager {

  private static final Logger log = Logger.getLogger(ServerEditorManager.class);

  private Map<IFile, Editor> openEditors = Collections.synchronizedMap(new LRUMap<>(10));
  private List<ISharedEditorListener> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void openEditor(IFile file, boolean activate) {
    try {
      getOrCreateEditor(file);
    } catch (IOException e) {
      log.warn("Could not open editor for " + file);
    }
  }

  @Override
  public Set<IFile> getOpenEditors() {
    return openEditors.keySet();
  }

  @Override
  public String getContent(IFile file) {
    try {
      return getOrCreateEditor(file).getContent();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String getNormalizedContent(IFile file) {
    String content = getContent(file);

    if (content == null) {
      return null;
    }

    String lineSeparator = TextPositionUtils.guessLineSeparator(content);

    return LineSeparatorNormalizationUtil.normalize(content, lineSeparator);
  }

  @Override
  public void saveEditors(IReferencePoint referencePoint) {
    // do nothing?
    // we do not keep dirty editors,
    // because the LRUMap might close Editors at any time
  }

  @Override
  public void adjustViewport(IFile file, LineRange range, TextSelection selection) {
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
   * Get an existing or create a new Editor for a given file. May remove the least recently used
   * Editor to free memory.
   *
   * @param file of the file to open
   * @return Editor of the file
   * @throws IOException
   */
  private Editor getOrCreateEditor(IFile file) throws IOException {
    Editor editor = openEditors.get(file);
    if (editor == null) {
      if (!file.exists()) {
        throw new NoSuchFileException(file.toString());
      }

      editor = new Editor(file);
      openEditors.put(file, editor);
    }
    return editor;
  }

  /**
   * Executes a text edit activity on the matching editor.
   *
   * @param activity the activity describing the text edit to apply
   */
  public void applyTextEdit(TextEditActivity activity) {
    IFile file = activity.getResource();
    try {
      Editor editor = getOrCreateEditor(file);
      editor.applyTextEdit(activity);
      editor.save();
      for (ISharedEditorListener listener : listeners) {
        listener.textEdited(activity);
      }
    } catch (IOException e) {
      log.error("Could not read " + file + " to apply text edit", e);
    }
  }

  /**
   * Updates the mapping of an open editor to a new file.
   *
   * @param oldFile the old file
   * @param newFile the new file
   */
  public void updateMapping(IFile oldFile, IFile newFile) {
    Editor oldEditor = openEditors.remove(oldFile);
    openEditors.put(newFile, oldEditor);
  }

  @Override
  public void closeEditor(IFile file) {
    openEditors.remove(file);
  }

  /**
   * Close all editors of files in a specific folder. Helpful if a folder gets deleted.
   *
   * @param folder the folder
   */
  public void closeEditorsInFolder(IFolder folder) {
    synchronized (openEditors) {
      ServerFolderImpl serverFolder = (ServerFolderImpl) folder;

      Set<IFile> keys = openEditors.keySet();
      Set<IFile> invalidKeys = new HashSet<>();
      for (IFile file : keys) {
        ServerFileImpl serverFile = (ServerFileImpl) file;

        if (serverFile.getFullPath().startsWith(serverFolder.getFullPath())) {
          invalidKeys.add(file);
        }
      }
      for (IFile file : invalidKeys) {
        closeEditor(file);
      }
    }
  }
}
