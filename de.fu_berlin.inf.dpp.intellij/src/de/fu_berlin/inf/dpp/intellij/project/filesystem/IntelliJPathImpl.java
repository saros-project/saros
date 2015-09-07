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

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import de.fu_berlin.inf.dpp.filesystem.IPath;

public class IntelliJPathImpl implements IPath {
    public static final String FILE_SEPARATOR = "/";

    private final String path;
    private final String[] segments;

    public IntelliJPathImpl(final String path) {

        this.path = path;
        String splitPath = path;

        // We must remove the first slash, otherwise String.split would
        // create an empty string as first segment
        if (path.startsWith("\\") || path.startsWith("/")) {
            splitPath = path.substring(1);
        }

        // "foo\bla.txt" is a valid linux path, which would not be handled
        // correctly by the separatorsToUnix. However, IntelliJ does not handle
        // these paths correctly and the FS Synchronizer logs an error when
        // encountering one. Thus it is not possible that IntelliJPathImpl is
        // called with
        // such a path and we can safely use this method.
        splitPath = FilenameUtils.separatorsToUnix(splitPath);
        segments = splitPath.split(Pattern.quote(FILE_SEPARATOR));
    }

    public IntelliJPathImpl(File file) {
        this(file.getPath());
    }

    @Override
    public IPath append(IPath path) {
        return this.path.endsWith(FILE_SEPARATOR) ? new IntelliJPathImpl(
            this.path + path.toPortableString()) : new IntelliJPathImpl(
            this.path + FILE_SEPARATOR + path.toPortableString());
    }

    @Override
    public String lastSegment() {
        return segments[segments.length - 1];
    }

    @Override
    public boolean hasTrailingSeparator() {
        return path.endsWith(FILE_SEPARATOR);
    }

    @Override
    public boolean isPrefixOf(IPath path) {
        return path.toString().startsWith(this.path);
    }

    @Override
    public int segmentCount() {
        return segments.length;
    }

    @Override
    public IPath removeLastSegments(int count) {
        String[] result = Arrays.copyOf(segments, segments.length - count);
        return new IntelliJPathImpl(join(result));
    }

    @Override
    public IPath removeFirstSegments(int count) {
        String[] result = Arrays.copyOfRange(segments, count, segments.length);

        return new IntelliJPathImpl(join(result));
    }

    @Override
    public boolean isEmpty() {
        return new File(path).exists();
    }

    @Override
    public String[] segments() {
        String[] segmentCopy = new String[segments.length];
        System.arraycopy(segments, 0, segmentCopy, 0, segments.length);
        return segmentCopy;
    }

    @Override
    public IPath append(String path) {
        return new IntelliJPathImpl(
            this.path.endsWith(FILE_SEPARATOR) ? this.path + path : this.path
                + FILE_SEPARATOR + path);
    }

    @Override
    public IPath addTrailingSeparator() {
        return path.endsWith(FILE_SEPARATOR) ? new IntelliJPathImpl(path)
            : new IntelliJPathImpl(path + FILE_SEPARATOR);

    }

    @Override
    public String getFileExtension() {
        String path = this.path;
        if (path.contains(".")) {
            path = path.substring(path.lastIndexOf("."));
        }

        return path;
    }

    @Override
    public IPath makeAbsolute() {
        return new IntelliJPathImpl(new File(path).getAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return new File(path).isAbsolute();
    }

    @Override
    public String toPortableString() {
        String path = this.path;

        if (path.contains("\\")) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    @Override
    public String toOSString() {
        return new File(path).getPath();
    }

    @Override
    public File toFile() {
        return new File(path);
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

    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        return this.path.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IntelliJPathImpl))
            return false;

        IntelliJPathImpl other = (IntelliJPathImpl) obj;

        return this.path.equalsIgnoreCase(other.path);
    }
}
