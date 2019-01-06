package de.fu_berlin.inf.dpp.editor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.User.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * The EditorPool manages the <code>IEditorParts</code> of the local user.
 *
 * <p>Currently only those parts are supported which can be traced to an {@link IFile} and an {@link
 * ITextViewer}.
 */
final class EditorPool {

  private static final Logger LOG = Logger.getLogger(EditorPool.class);

  /*
   * The values might change over time but we need the original state when an
   * editor part is removed because we *added* it with these values.
   */

  /** Holder class to hold input references of <code>IEditorPart</code>s. */
  private static class EditorPartInputReferences {
    private final IEditorInput input;
    private final IFile file;

    private EditorPartInputReferences(final IEditorInput input, final IFile file) {
      this.input = input;
      this.file = file;
    }
  }

  private final EditorManager editorManager;

  private final DirtyStateListener dirtyStateListener;

  private final StoppableDocumentListener documentListener;

  /**
   * The editorParts-map will return all EditorParts associated with a given SPath. This can be
   * potentially many because a IFile (which can be identified using a IPath) can be opened in
   * multiple editors.
   */
  private Map<SPath, Set<IEditorPart>> editorParts = new HashMap<SPath, Set<IEditorPart>>();

  /**
   * The editorInputMap contains all IEditorParts which are managed by the EditorPool and stores the
   * associated input references.
   */
  private final Map<IEditorPart, EditorPartInputReferences> editorInputMap =
      new HashMap<IEditorPart, EditorPartInputReferences>();

  private final Map<IEditorPart, EditorListener> editorListeners =
      new HashMap<IEditorPart, EditorListener>();

  /** Editors where the user isn't allowed to write */
  private final List<IEditorPart> lockedEditors = new ArrayList<IEditorPart>();

  EditorPool(EditorManager editorManager) {
    this.editorManager = editorManager;
    this.dirtyStateListener = new DirtyStateListener(editorManager);
    this.documentListener = new StoppableDocumentListener(editorManager);
  }

  /**
   * Adds an {@link IEditorPart} to the pool. This method also connects the editorPart with its data
   * source (identified by associated {@link IFile}), makes it editable for user with {@link
   * Permission#WRITE_ACCESS}, and registers listeners:
   *
   * <ul>
   *   <li>{@link IElementStateListener} on {@link IDocumentProvider} - listens for the changes in
   *       the file connected with the editor (e.g. file gets 'dirty')
   *   <li>{@link IDocumentListener} on {@link IDocument} - listens for changes in the document
   *       (e.g. documents text gets changed)
   *   <li>{@link EditorListener} on {@link IEditorPart} - listens for basic events needed for
   *       tracking of text selection and viewport changes (e.g. mouse events, keyboard events)
   * </ul>
   *
   * This method will return without any effect if the given IEditorPart does not a.) represent an
   * IFile, b.) which can be referred to using an IPath and c.) the IEditorPart can be mapped to an
   * ITextViewer.
   */
  public void add(final IEditorPart editorPart) {
    LOG.trace("adding editor part " + editorPart + " [" + editorPart.getTitle() + "]");

    if (isManaged(editorPart)) {
      LOG.error("editor part " + editorPart + " is already managed");
      return;
    }

    final ITextViewer viewer = EditorAPI.getViewer(editorPart);

    if (viewer == null) {
      LOG.warn("editor part is not a ITextViewer: " + editorPart);
      return;
    }

    final IEditorInput input = editorPart.getEditorInput();
    final IFile file = ResourceUtil.getFile(input);

    if (file == null) {
      LOG.warn("editor part does not use a file storage: " + editorPart);
      return;
    }

    /*
     * Connecting causes Conversion of Delimiters which trigger Selection
     * and Save Activities, so connect before adding listeners
     */
    editorManager.connect(file);

    final EditorListener listener = new EditorListener(editorManager);
    listener.bind(editorPart);

    /*
     * OMG ... either pull this call out of this class or access the
     * editorManager variables in a better manner
     */
    setEditable(editorPart, editorManager.hasWriteAccess && !editorManager.isLocked);

    final IDocumentProvider documentProvider = EditorAPI.getDocumentProvider(input);

    dirtyStateListener.register(documentProvider, input);
    documentProvider.getDocument(input).addDocumentListener(documentListener);

    IReferencePoint referencePoint = EclipseReferencePointManager.create(file);
    IPath referencePointRelativePath = file.getProjectRelativePath();

    final SPath path =
        new SPath(referencePoint, ResourceAdapterFactory.create(referencePointRelativePath));

    Set<IEditorPart> parts = editorParts.get(path);

    if (parts == null) {
      parts = new HashSet<IEditorPart>();
      editorParts.put(path, parts);
    }

    editorInputMap.put(editorPart, new EditorPartInputReferences(input, file));

    editorListeners.put(editorPart, listener);
    parts.add(editorPart);
  }

  private void setEditable(IEditorPart editorPart, boolean newIsEditable) {
    boolean isEditable = !lockedEditors.contains(editorPart);

    // Already as we want it?
    if (newIsEditable == isEditable) return;

    if (newIsEditable) lockedEditors.remove(editorPart);
    else lockedEditors.add(editorPart);

    LOG.trace(editorPart.getEditorInput().getName() + " set to editable: " + newIsEditable);

    EditorAPI.setEditable(editorPart, newIsEditable);
  }

  /**
   * Returns the {@linkplain SPath path} for the corresponding editor.
   *
   * @return the path for the corresponding editor or <code>null</code> if the editor is not managed
   *     by this pool
   */
  public SPath getPath(final IEditorPart editorPart) {
    if (!isManaged(editorPart)) return null;

    for (final Entry<SPath, Set<IEditorPart>> entry : editorParts.entrySet()) {

      if (entry.getValue().contains(editorPart)) return entry.getKey();
    }

    // assert false : should never been reached
    return null;
  }

  /**
   * Removes an {@link IEditorPart} from the pool and makes the editor editable again.
   *
   * <p>This Method also disconnects the editorPart from its data source (identified by associated
   * {@link IFile}) and removes registered listeners:
   *
   * <ul>
   *   <li>{@link IElementStateListener} from {@link IDocumentProvider}
   *   <li>{@link IDocumentListener} from {@link IDocument}
   *   <li>{@link EditorListener} from {@link IEditorPart}
   * </ul>
   *
   * @param editorPart editorPart to be removed
   */
  public void remove(final IEditorPart editorPart) {
    LOG.trace("removing editor part " + editorPart + " [" + editorPart.getTitle() + "]");

    if (!isManaged(editorPart)) {
      LOG.error("editor part is not managed: " + editorPart);
      return;
    }

    final EditorPartInputReferences inputRefs = editorInputMap.remove(editorPart);

    final IEditorInput input = inputRefs.input;
    final IFile file = inputRefs.file;

    // Unregister and unhook
    setEditable(editorPart, true);

    editorListeners.remove(editorPart).unbind();

    final IDocumentProvider documentProvider = EditorAPI.getDocumentProvider(input);

    dirtyStateListener.unregister(documentProvider, input);

    final IDocument document = documentProvider.getDocument(input);

    if (document == null) {
      LOG.warn("could not disconnect from document: " + file);
    } else {
      document.removeDocumentListener(documentListener);
    }

    editorManager.disconnect(file);

    IReferencePoint referencePoint = EclipseReferencePointManager.create(file);
    IPath referencePointRelativePath = file.getProjectRelativePath();

    final SPath path =
        new SPath(referencePoint, ResourceAdapterFactory.create(referencePointRelativePath));

    editorParts.get(path).remove(editorPart);
  }

  /**
   * Returns all IEditorParts which have been added to this pool which display a file using the
   * given path.
   *
   * @param path {@link IPath} of the Editor
   * @return set of relating IEditorPart
   */
  public Set<IEditorPart> getEditors(final SPath path) {
    final HashSet<IEditorPart> result = new HashSet<IEditorPart>();

    if (editorParts.containsKey(path)) result.addAll(editorParts.get(path));

    return result;
  }

  /**
   * Returns all IEditorParts actually managed by this pool.
   *
   * @return set of all {@link IEditorPart} from the {@link EditorPool}.
   */
  public Set<IEditorPart> getAllEditors() {
    final Set<IEditorPart> result = new HashSet<IEditorPart>();

    for (final Set<IEditorPart> parts : editorParts.values()) result.addAll(parts);

    return result;
  }

  /** Removes all {@link IEditorPart} from the EditorPool. */
  public void removeAllEditors() {
    LOG.trace("removing all editors");

    for (final IEditorPart part : new HashSet<IEditorPart>(getAllEditors())) remove(part);

    dirtyStateListener.unregisterAll();

    assert getAllEditors().size() == 0;
  }

  /** Changes the editable state of all editors currently managed by this pool. */
  public void setEditable(final boolean editable) {
    for (final IEditorPart editorPart : getAllEditors()) setEditable(editorPart, editable);
  }

  /** Returns if the given <code>IEditorPart</code> is managed by this pool. */
  public boolean isManaged(final IEditorPart editorPart) {
    return editorInputMap.containsKey(editorPart);
  }

  /**
   * Changes the state of the <code>ElementStateListener</code> for <b>all</b> editors in this pool.
   *
   * @param enabled if <code>true</code> element state changes will be reported, otherwise no
   *     element state changes will be reported
   * @see #add(IEditorPart)
   */
  public void setElementStateListenerEnabled(final boolean enabled) {
    dirtyStateListener.setEnabled(enabled);
  }

  /**
   * Changes the state of the <code>DocumentListener</code> for <b>all</b> editors in this pool.
   *
   * @param enabled if <code>true</code> document changes will be reported, otherwise no document
   *     changes will be reported
   * @see #add(IEditorPart)
   */
  public void setDocumentListenerEnabled(final boolean enabled) {
    documentListener.setEnabled(enabled);
  }
}
