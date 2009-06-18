package de.fu_berlin.inf.dpp.util;

import java.util.Random;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

public class UtilTest extends TestCase {
    public void testInflateDeflate() {
        String shortString = "foobar";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 1 << 20; i++) {
            sb.append(rand.nextInt() % 10);
        }
        String longString = sb.toString();

        byte[] compressedShort = Util.deflate(shortString.getBytes(),
            SubMonitor.convert(new NullProgressMonitor()));
        String shortResult = new String(Util.inflate(compressedShort,
            SubMonitor.convert(new NullProgressMonitor())));
        byte[] compressedLong = Util.deflate(longString.getBytes(), SubMonitor
            .convert(new NullProgressMonitor()));
        String longResult = new String(Util.inflate(compressedLong, SubMonitor
            .convert(new NullProgressMonitor())));

        assertEquals(shortString, shortResult);
        assertEquals(longString, longResult);
    }
}
