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

import de.fu_berlin.inf.dpp.filesystem.IPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathImp implements IPath {
    public static final String FILE_SEPARATOR = "/";

    private String _path;

    public PathImp(String path) {

        if (path.startsWith("file:/") || path.startsWith("file:\\")) {
            path = path.substring("file:/".length());
        }

        if (path.startsWith("\\") || path.startsWith("/")) {
            path = path.substring(1);
        }

        this._path = path;
        _path = toPortableString();
    }

    public PathImp(File file) {
        this(file.getPath());
    }

    @Override
    public IPath append(IPath path) {
        return _path.endsWith(FILE_SEPARATOR) ?
            new PathImp(_path + path.toPortableString()) :
            new PathImp(_path + FILE_SEPARATOR + path.toPortableString());
    }

    @Override
    public String lastSegment() {
        String[] segments = _path.split(FILE_SEPARATOR);
        return segments[segments.length - 1];
    }

    @Override
    public boolean hasTrailingSeparator() {
        return _path.endsWith(FILE_SEPARATOR);
    }

    @Override
    public boolean isPrefixOf(IPath path) {
        return path.toString().startsWith(_path);
    }

    @Override
    public int segmentCount() {
        return _path.split(FILE_SEPARATOR).length;
    }

    @Override
    public IPath removeLastSegments(int count) {
        String[] segments = _path.split(FILE_SEPARATOR);
        segments = Arrays.copyOf(segments, segments.length - count);

        return new PathImp(join(segments));
    }

    public IPath removeFirstSegments(int count) {
        String[] segments = _path.split(FILE_SEPARATOR);
        segments = Arrays.copyOfRange(segments, count, segments.length);

        return new PathImp(join(segments));
    }

    @Override
    public boolean isEmpty() {
        return new File(_path).exists();
    }

    @Override
    public String[] segments() {
        String[] array = _path.split(FILE_SEPARATOR);
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < array.length; i++) {
            String segment = array[i];
            if (!segment.isEmpty()) {
                list.add(segment);
            }
        }

        return list.toArray(new String[] { });
    }

    @Override
    public IPath append(String path) {
        return new PathImp(_path.endsWith(FILE_SEPARATOR) ?
            _path + path :
            _path + FILE_SEPARATOR + path);
    }

    @Override
    public IPath addTrailingSeparator() {
        return _path.endsWith(FILE_SEPARATOR) ?
            new PathImp(_path) :
            new PathImp(_path + FILE_SEPARATOR);

    }

    @Override
    public IPath addFileExtension(String extension) {
        return new PathImp(_path + "." + extension);
    }

    @Override
    public IPath removeFileExtension() {
        String path = _path;
        if (path.contains(".")) {
            path = path.substring(0, path.lastIndexOf("."));
        }
        return new PathImp(path);
    }

    @Override
    public String getFileExtension() {
        String path = _path;
        if (path.contains(".")) {
            path = path.substring(path.lastIndexOf("."));
        }

        return path;
    }

    @Override
    public IPath makeAbsolute() {
        return new PathImp(new File(_path).getAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return new File(_path).isAbsolute();
    }

    @Override
    public String toPortableString() {
        String path = _path;

        if (path.contains("\\")) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    @Override
    public String toOSString() {
        return new File(_path).getPath();
    }

    @Override
    public File toFile() {
        return new File(_path);
    }

    private String join(String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            if (i >= data.length - 1) {
                break;
            }
            sb.append(FILE_SEPARATOR);
        }
        return sb.toString();
    }

    public String toString() {
        return _path;
    }

    @Override
    public int hashCode() {
        return this._path.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PathImp)) {
            return false;
        }

        PathImp other = (PathImp) obj;

        return this._path.equalsIgnoreCase(other._path);
    }
}
