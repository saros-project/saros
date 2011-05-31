package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class GZipTest {

    /**
     * Not really a test, just to make sure that we use the GZIPStreams
     * correctly
     */
    @Test
    public void testGZIP() throws IOException {

        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 6, 7, 8, 89, 8, 9, 9, 9 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        IOUtils.write(data, gos);
        gos.finish();
        IOUtils.closeQuietly(gos);

        assertTrue(Arrays.equals(IOUtils.toByteArray(new GZIPInputStream(
            new ByteArrayInputStream(bos.toByteArray()))), data));
    }

}
