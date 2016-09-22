/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.negotiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * TODO [TEST] Add Testcases for non-existing files florianthiel: Does FileList
 * care about existence of files?
 * 
 * TODO [TEST] Add Testcases for derived files florianthiel: What are
 * "derived files" in this context?
 * 
 * FIXME FileList now uses IResource.getProject(), which isn't implemented yet
 * by FileStub, so any test
 */
public class FileListTest extends AbstractFileListTest {

    private static XStream xstream = new XStream();

    static {
        xstream.registerConverter(BooleanConverter.BINARY);
        xstream.processAnnotations(FileList.class);
    }

    @Test
    public void testGetEncodings() {
        Set<String> encodings = threeEntryList.getEncodings();

        Set<String> expectedEncodings = new HashSet<String>(Arrays.asList(
            "ISO-8859-1", "UTF-16"));

        assertEquals("not all encodings were fetched", expectedEncodings,
            encodings);
    }

    @Test
    public void testGetFilePaths() throws Exception {
        List<String> paths = threeEntryList.getPaths();

        assertPaths(paths, ROOT1, ROOT2, SUBDIR_FILE1);
    }

    @Test
    public void testEquals() throws IOException {
        FileList sameFileList = FileListFactory.createFileList(null,
            threeFileList, null, null);
        assertEquals(threeEntryList, sameFileList);
        assertEquals(emptyFileList, emptyFileList);

        assertFalse(threeEntryList.equals(fourEntryList));
        assertFalse(emptyFileList.equals(threeEntryList));
    }

    @Test
    public void testRoundtripSerialization() {
        FileList replicated = fromXML(toXML(threeEntryList));
        assertEquals(threeEntryList, replicated);
    }

    @Test
    public void testToXmlAndBack() throws Exception {
        List<String> files = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int a = 0; a < 2; a++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    builder.setLength(0);
                    builder.append("string12345");
                    for (int j = 0; j < 5; j++) {
                        builder.append((char) (random.nextInt(26) + 65));
                    }
                    files.add("foo1234567890" + i + "/bar1234567890" + a + "/"
                        + builder.toString());
                }
            }
        }

        FileList list = FileListFactory.createFileList(files);
        String xml = toXML(list);
        FileList listFromXml = fromXML(xml);
        assertEquals(list, listFromXml);
    }

    private String toXML(FileList list) {
        StringWriter writer = new StringWriter(512 * 1024);
        xstream.marshal(list, new CompactWriter(writer));
        return writer.toString();
    }

    private static FileList fromXML(String xml) {
        return (FileList) xstream.fromXML(xml);
    }
}
