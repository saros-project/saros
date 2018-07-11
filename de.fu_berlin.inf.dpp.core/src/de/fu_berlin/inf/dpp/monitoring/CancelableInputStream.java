package de.fu_berlin.inf.dpp.monitoring;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps an <code>InputStream</code> to enable user canceling of long
 * taking processings. This works best if consumers read streams in small
 * buffers, like Eclipse does.
 */
public class CancelableInputStream extends InputStream {
    /** the wrapped input stream */
    private final InputStream in;

    /** the monitor to check */
    private final IProgressMonitor monitor;

    /**
     * Creates a new <code>CancelableInputStream</code> that wraps the given
     * input and checks for a canceled monitor at every read call.
     * 
     * @param in
     *            The wrapped input stream
     * @param monitor
     *            The checked monitor
     */
    public CancelableInputStream(InputStream in, IProgressMonitor monitor) {
        this.in = in;
        this.monitor = monitor;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (monitor.isCanceled())
            throw new IOException("Processing was canceled!");

        return super.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        if (monitor.isCanceled())
            throw new IOException("Processing was canceled!");

        return in.read();
    }
}
