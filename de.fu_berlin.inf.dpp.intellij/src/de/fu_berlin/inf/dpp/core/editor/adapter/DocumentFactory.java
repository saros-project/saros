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

package de.fu_berlin.inf.dpp.core.editor.adapter;

import com.intellij.openapi.editor.Document;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.ResourceConverter;

/**
 * Class for creating IDocuments from IFiles using IntelliJ Document implementation.
 */
public class DocumentFactory {

    /**
     * @param file
     * @return an IDocument created from the file or <code>null</code>, if the
     * corresponding file does not exist.
     */
    public static IDocument getDocument(IFile file) {
        Document nativeDocument = ResourceConverter
            .getDocument(file.getFullPath().toFile());
        return nativeDocument == null ?
            null :
            new DocumentAdapter(nativeDocument);
    }
}
