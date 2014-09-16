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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFileContentChangedListener;
import de.fu_berlin.inf.dpp.filesystem.IFileContentChangedNotifier;

/**
 * Class provide bridge to IntelliJ virtual file listener, supposed to be used
 * in common engine classes
 */
public class FileContentChangedNotifierBridge implements
    IFileContentChangedNotifier, VirtualFileListener {
    private List<IFileContentChangedListener> listeners = new CopyOnWriteArrayList<IFileContentChangedListener>();

    @Override
    public void addFileContentChangedListener(
        IFileContentChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeFileContentChangedListener(
        IFileContentChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(@NotNull VirtualFileEvent virtualFileEvent) {
        IFile file = new FileImp(null, new File(virtualFileEvent.getFile()
            .getPath()));

        for (IFileContentChangedListener listener : listeners) {
            listener.fileContentChanged(file);
        }
    }

    @Override
    public void propertyChanged(
        @NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {
        // NOP
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
        notifyListeners(virtualFileEvent);
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        notifyListeners(virtualFileEvent);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        notifyListeners(virtualFileEvent);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        notifyListeners(virtualFileMoveEvent);
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
        notifyListeners(virtualFileCopyEvent);
    }

    @Override
    public void beforePropertyChange(
        @NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {
        // NOP
    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {
        // NOP
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {
        // NOP
    }

    @Override
    public void beforeFileMovement(
        @NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        notifyListeners(virtualFileMoveEvent);
    }
}
