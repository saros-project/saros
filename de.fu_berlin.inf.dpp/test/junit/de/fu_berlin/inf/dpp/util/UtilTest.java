package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.junit.Test;

public class UtilTest {
    @Test
    public void testInflateDeflate() throws IOException {
        String shortString = "foobar";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 1 << 20; i++) {
            sb.append(rand.nextInt() % 10);
        }
        String longString = sb.toString();

        byte[] compressedShort = Utils.deflate(shortString.getBytes(),
            SubMonitor.convert(new NullProgressMonitor()));
        String shortResult = new String(Utils.inflate(compressedShort,
            SubMonitor.convert(new NullProgressMonitor())));
        byte[] compressedLong = Utils.deflate(longString.getBytes(),
            SubMonitor.convert(new NullProgressMonitor()));
        String longResult = new String(Utils.inflate(compressedLong,
            SubMonitor.convert(new NullProgressMonitor())));

        assertEquals(shortString, shortResult);
        assertEquals(longString, longResult);
    }
}
