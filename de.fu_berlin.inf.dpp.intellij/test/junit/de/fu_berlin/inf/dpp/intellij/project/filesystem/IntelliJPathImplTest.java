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

import org.junit.Test;

import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IntelliJPathImplTest {

    @Test
    public void parsesLinuxPathCorrectly() {
        String stringPath = "/home/user/saros/project";

        IntelliJPathImpl path = new IntelliJPathImpl(stringPath);

        String[] expected = { "home", "user", "saros", "project" };

        assertArrayEquals(path.segments(), expected);
        assertEquals(path.toString(), stringPath);
    }

    @Test
    public void parsesLinuxPathWithTrailingSlashCorrectly() {
        String stringPath = "/home/user/saros/project/";
        IntelliJPathImpl path = new IntelliJPathImpl(stringPath);

        String[] expected = { "home", "user", "saros", "project" };

        assertArrayEquals(path.segments(), expected);
        assertEquals(path.toString(), stringPath);
    }

    @Test
    public void parsesWindowsPathCorrectly() {
        String stringPath = "C:\\Users\\user\\saros\\project";
        IntelliJPathImpl path = new IntelliJPathImpl(stringPath);

        String[] expected = { "C:", "Users", "user", "saros", "project" };

        assertArrayEquals(path.segments(), expected);
        assertEquals(path.toString(), stringPath);
    }

    @Test
    public void parsesWindowsPathWithBeginningAndTrailingSlashCorrectly() {
        String stringPath = "\\C:\\Users\\user\\saros\\project\\";
        IntelliJPathImpl path = new IntelliJPathImpl(stringPath);

        String[] expected = { "C:", "Users", "user", "saros", "project" };

        assertArrayEquals(path.segments(), expected);
        assertEquals(path.toString(), stringPath);
    }
}