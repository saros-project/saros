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

package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SwtLibLoaderTest {

    private static final String JAR_FILENAME_OSX_64 = "swt-4.4-cocoa-macosx-x86_64.jar";
    private static final String JAR_FILENAME_OSX_32 = "swt-4.4-cocoa-macosx-x86.jar";
    private static final String JAR_FILENAME_LINUX_64 = "swt-4.4-gtk-linux-x86_64.jar";
    private static final String JAR_FILENAME_LINUX_32 = "swt-4.4-gtk-linux-x86.jar";
    private static final String JAR_FILENAME_WIN_64 = "swt-4.4-win32-x86_64.jar";
    private static final String JAR_FILENAME_WIN_32 = "swt-4.4-win32-x86.jar";

    @Test
    public void createJarFilename() {
        assertEquals(JAR_FILENAME_OSX_32,
            SwtLibLoader.getJarFilename("mac", "86"));
        assertEquals(JAR_FILENAME_OSX_64,
            SwtLibLoader.getJarFilename("mac", "64"));
        assertEquals(JAR_FILENAME_LINUX_32,
            SwtLibLoader.getJarFilename("linux", "86"));
        assertEquals(JAR_FILENAME_LINUX_64,
            SwtLibLoader.getJarFilename("linux", "64"));
        assertEquals(JAR_FILENAME_WIN_32,
            SwtLibLoader.getJarFilename("win", "86"));
        assertEquals(JAR_FILENAME_WIN_64,
            SwtLibLoader.getJarFilename("win", "64"));
    }
}