package de.fu_berlin.inf.dpp.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple delegator class which won't forward calls to {@link #close()}.
 */
public class UncloseableInputStream extends InputStream {

    protected InputStream closeableInputStream;

    public UncloseableInputStream(InputStream closeableInputStream) {
        super();
        this.closeableInputStream = closeableInputStream;
    }

    @Override
    public int available() throws IOException {
        return closeableInputStream.available();
    }

    @Override
    public void close() throws IOException {
        // NOP
    }

    @Override
    public boolean equals(Object obj) {
        return closeableInputStream.equals(obj);
    }

    @Override
    public int hashCode() {
        return closeableInputStream.hashCode();
    }

    @Override
    public void mark(int readlimit) {
        closeableInputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return closeableInputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        return closeableInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return closeableInputStream.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return closeableInputStream.read(b);
    }

    @Override
    public void reset() throws IOException {
        closeableInputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return closeableInputStream.skip(n);
    }

    @Override
    public String toString() {
        return closeableInputStream.toString();
    }
}
