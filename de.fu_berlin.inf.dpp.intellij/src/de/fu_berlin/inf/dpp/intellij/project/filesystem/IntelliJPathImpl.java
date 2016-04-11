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

import de.fu_berlin.inf.dpp.filesystem.IPath;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO:
 * This is a copy of the server implementation of the path interface.
 * As soon as the core is Java 7 we can merge the two.
 */
public class IntelliJPathImpl implements IPath {
    private Path delegate;
    private boolean trailingSeparator;

    public static final IPath EMPTY = new IntelliJPathImpl(Paths.get(""),
        false);

    public static IPath fromString(String pathString) {
        if (pathString == null || pathString.isEmpty()) {
            return EMPTY;
        }
        return new IntelliJPathImpl(Paths.get(pathString),
            pathString.endsWith("/") || pathString.endsWith(File.separator));
    }

    private IntelliJPathImpl(Path delegate, boolean trailingSeparator) {
        /*
         * OpenJDK 7 on Linux has a bug which causes normalize() to throw an
         * ArrayIndexOutOfBoundsException if called on the empty path.
         */
        this.delegate = delegate.equals(Paths.get("")) ?
            delegate :
            delegate.normalize();
        this.trailingSeparator = trailingSeparator;
    }

    @Override
    public String[] segments() {
        String[] segments = new String[segmentCount()];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = delegate.getName(i).toString();
        }
        return segments;
    }

    @Override
    public int segmentCount() {
        return (this == EMPTY) ? 0 : delegate.getNameCount();
    }

    @Override
    public String segment(int index) {
        return delegate.getName(index).toString();
    }

    @Override
    public String lastSegment() {
        return (this == EMPTY) ? null : delegate.getFileName().toString();
    }

    @Override
    public IPath removeFirstSegments(int count) {
        if (count == 0) {
            return this;
        } else if (count >= segmentCount()) {
            return EMPTY;
        }
        Path newDelegate = delegate.subpath(count, delegate.getNameCount());
        return new IntelliJPathImpl(newDelegate, hasTrailingSeparator());
    }

    @Override
    public IPath removeLastSegments(int count) {
        if (count == 0) {
            return this;
        } else if (count >= segmentCount()) {
            return EMPTY;
        }

        int newCount = delegate.getNameCount() - count;
        Path newDelegate = delegate.subpath(0, newCount);

        /*
         * java.nio.Path#subpath always returns a relative path, but we want an
         * absolute path with last segments removed to still be absolute.
         */
        if (isAbsolute()) {
            newDelegate = delegate.getRoot().resolve(newDelegate);
        }

        return new IntelliJPathImpl(newDelegate, hasTrailingSeparator());
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public IPath makeAbsolute() {
        if (isAbsolute()) {
            return this;
        }
        Path root = delegate.toAbsolutePath().getRoot();
        Path newDelegate = root.resolve(delegate);
        return new IntelliJPathImpl(newDelegate, hasTrailingSeparator());
    }

    @Override
    public boolean hasTrailingSeparator() {
        return trailingSeparator;
    }

    @Override
    public boolean isPrefixOf(IPath path) {
        if (this == EMPTY) {
            return true;
        }
        Path other = Paths.get(path.toOSString());
        return other.startsWith(delegate);
    }

    @Override
    public IPath append(IPath path) {
        Path other = Paths.get(path.toOSString());

        /*
         * Appending an absolute path should simply append the path's segments
         * and ignore everything else. To make this work with
         * java.nio.Path#resolve, we need to make the path relative first.
         */
        if (other.isAbsolute()) {
            other = other.getRoot().relativize(other);
        }

        Path newDelegate = delegate.resolve(other);
        return new IntelliJPathImpl(newDelegate, path.hasTrailingSeparator());
    }

    @Override
    public IPath append(String pathString) {
        return append(IntelliJPathImpl.fromString(pathString));
    }

    @Override
    public String toPortableString() {
        return toOSString().replace(File.separator, "/");
    }

    @Override
    public String toOSString() {
        String pathString = delegate.toString();
        if (hasTrailingSeparator()) {
            pathString += File.separator;
        }
        return pathString;
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IntelliJPathImpl)) {
            return false;
        }
        IntelliJPathImpl other = (IntelliJPathImpl) o;
        return delegate.equals(other.delegate)
            && trailingSeparator == other.trailingSeparator;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
