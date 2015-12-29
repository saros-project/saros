/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

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
