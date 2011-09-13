package de.fu_berlin.inf.dpp.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream decorator class which won't forward calls to {@link #close()}.
 */
public class UncloseableInputStream extends FilterInputStream {

    public UncloseableInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException {
        // prevent close
    }
}
