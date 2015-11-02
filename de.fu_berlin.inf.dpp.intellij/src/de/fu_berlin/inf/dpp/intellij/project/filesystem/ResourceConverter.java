package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;

import java.io.File;

/**
 * Converts Resources from plain files and Saros paths to IntelliJ documents and VirtualFiles.
 */
public class ResourceConverter {

    private ResourceConverter() {
    }

    private static final LocalFileSystem localFileSystem = LocalFileSystem
        .getInstance();
    private static final FileDocumentManager fileDocumentManager = FileDocumentManager
        .getInstance();

    public static Document getDocument(final File file) {
        return fileDocumentManager.getDocument(toVirtualFile(file));
    }

    public static VirtualFile toVirtualFile(SPath path) {
        return toVirtualFile(path.getFile().getLocation().toFile());
    }

    private static VirtualFile toVirtualFile(File path) {
        return localFileSystem.refreshAndFindFileByIoFile(path);
    }
}
