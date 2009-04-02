package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.TextFileBufferOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.optional.cdt.CDTFacade;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;

/**
 * Static utility methods for working with Eclipse Editors
 */
public class EditorUtils {

    public static IDocumentProvider getDocumentProvider(IEditorInput input) {

        Object adapter = input.getAdapter(IFile.class);
        if (adapter != null) {
            IFile file = (IFile) adapter;

            String fileExtension = file.getFileExtension();

            if (fileExtension != null) {
                if (fileExtension.equals("java")) {
                    // TODO Rather this dependency should be injected when the
                    // EditorAPI is created itself.
                    JDTFacade facade = Saros.getDefault().getContainer()
                        .getComponent(JDTFacade.class);

                    if (facade.isJDTAvailable()) {
                        return facade.getDocumentProvider();
                    }

                } else if (fileExtension.equals("c")
                    || fileExtension.equals("h") || fileExtension.equals("cpp")
                    || fileExtension.equals("cxx")
                    || fileExtension.equals("hxx")) {

                    // TODO Rather this dependency should be injected when the
                    // EditorAPI is created itself.
                    CDTFacade facade = Saros.getDefault().getContainer()
                        .getComponent(CDTFacade.class);

                    if (facade.isCDTAvailable()) {
                        return facade.getDocumentProvider();
                    }
                }
            }
        }

        DocumentProviderRegistry registry = DocumentProviderRegistry
            .getDefault();
        return registry.getDocumentProvider(input);
    }

    public static IDocument getDocument(IEditorPart editorPart) {
        IEditorInput input = editorPart.getEditorInput();

        return getDocumentProvider(input).getDocument(input);
    }

    /**
     * Returns the TextFileBuffer associated with this project relative path OR
     * null if the path could not be traced to a Buffer.
     * 
     * @param docPath
     *            A project relative path
     * @return
     */
    protected static ITextFileBuffer getTextFileBuffer(IResource resource) {

        IPath fullPath = resource.getFullPath();

        ITextFileBufferManager tfbm = FileBuffers.getTextFileBufferManager();

        ITextFileBuffer fileBuff = tfbm.getTextFileBuffer(fullPath,
            LocationKind.IFILE);
        if (fileBuff != null)
            return fileBuff;
        else {
            try {
                tfbm.connect(fullPath, LocationKind.IFILE,
                    new NullProgressMonitor());
            } catch (CoreException e) {
                EditorManager.log
                    .error("Could not connect to file " + fullPath);
                return null;
            }
            return tfbm.getTextFileBuffer(fullPath, LocationKind.IFILE);
        }
    }

    public static void convertLineDelimiters(IFile file) {

        EditorManager.log.debug("Converting line delimiters...");

        boolean makeReadable = false;

        ResourceAttributes resourceAttributes = file.getResourceAttributes();
        if (resourceAttributes.isReadOnly()) {
            resourceAttributes.setReadOnly(false);
            try {
                file.setResourceAttributes(resourceAttributes);
                makeReadable = true;
            } catch (CoreException e) {
                EditorManager.log.error(
                    "Error making file readable for delimiter conversion:", e);
            }
        }
        // Now run the conversion operation
        IPath[] paths = new IPath[] { file.getFullPath() };

        ITextFileBufferManager buffManager = FileBuffers
            .getTextFileBufferManager();

        // convert operation to change line delimiters
        TextFileBufferOperation convertOperation = new ConvertLineDelimitersOperation(
            "\n");

        // operation runner for the convert operation
        FileBufferOperationRunner runner = new FileBufferOperationRunner(
            buffManager, null);

        // execute convert operation in runner
        try {
            // FIXME #2671663: Converting Line Delimiters causes Save
            runner.execute(paths, convertOperation, new NullProgressMonitor());
        } catch (OperationCanceledException e) {
            EditorManager.log.error("Can't convert line delimiters:", e);
        } catch (CoreException e) {
            EditorManager.log.error("Can't convert line delimiters:", e);
        }

        if (makeReadable) {
            resourceAttributes.setReadOnly(true);
            try {
                file.setResourceAttributes(resourceAttributes);
            } catch (CoreException e) {
                EditorManager.log
                    .error(
                        "Error restoring readable state to false after delimiter conversion:",
                        e);
            }
        }
    }

}
