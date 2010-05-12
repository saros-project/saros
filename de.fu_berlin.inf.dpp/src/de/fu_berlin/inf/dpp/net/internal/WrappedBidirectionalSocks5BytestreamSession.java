package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamSession;

public class WrappedBidirectionalSocks5BytestreamSession implements
    BytestreamSession {

    private static Logger log = Logger
        .getLogger(WrappedBidirectionalSocks5BytestreamSession.class);

    protected Socks5BytestreamSession in;
    protected Socks5BytestreamSession out;

    public WrappedBidirectionalSocks5BytestreamSession(
        Socks5BytestreamSession in, Socks5BytestreamSession out) {
        this.in = in;
        this.out = out;

    }

    public void close() throws IOException {
        IOException e = null;

        try {
            in.close();
        } catch (IOException e1) {
            e = e1;
        }

        try {
            out.close();
        } catch (IOException e1) {
            e = e1;
        }
        if (e != null)
            throw e;
    }

    public InputStream getInputStream() throws IOException {
        return in.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return out.getOutputStream();
    }

    public int getReadTimeout() throws IOException {
        int inTO = in.getReadTimeout();

        // if (inTO != out.getReadTimeout())
        // log.warn("inconsistent Timeouts");

        return inTO;
    }

    public void setReadTimeout(int timeout) throws IOException {
        in.setReadTimeout(timeout);
        // would this make sense?
        // out.setReadTimeout(timeout);
    }

}
