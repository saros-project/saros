package de.fu_berlin.inf.dpp.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public class Util {

    /**
     * Obtain a free port we can use.
     * 
     * @return A free port number.
     */
    public static int getFreePort() {
        ServerSocket ss;
        int freePort = 0;

        for (int i = 0; i < 10; i++) {
            freePort = (int) (10000 + Math.round(Math.random() * 10000));
            freePort = freePort % 2 == 0 ? freePort : freePort + 1;
            try {
                ss = new ServerSocket(freePort);
                freePort = ss.getLocalPort();
                ss.close();
                return freePort;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ss = new ServerSocket(0);
            freePort = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return freePort;
    }

    public static void close(Socket socketToClose) {
        if (socketToClose == null)
            return;

        try {
            socketToClose.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static void close(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static <T> Callable<T> delay(final int milliseconds,
        final Callable<T> callable) {
        return new Callable<T>() {
            public T call() throws Exception {

                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                return callable.call();
            }
        };
    }

    public static <T> Callable<T> retryEvery500ms(final Callable<T> callable) {
        return new Callable<T>() {
            public T call() {
                T t = null;
                while (t == null && !Thread.currentThread().isInterrupted()) {
                    try {
                        t = callable.call();
                    } catch (InterruptedIOException e) {
                        // Workaround for bug in Limewire RUDP
                        // https://www.limewire.org/jira/browse/LWC-2838
                        return null;
                    } catch (Exception e) {
                        // Log here for connection problems.
                        t = null;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                            return null;
                        }
                    }
                }
                return t;
            }
        };
    }

    /**
     * Returns an iterable which will return the given iterator ONCE.
     * 
     * Subsequent calls to iterator() will throw an IllegalStateException.
     * 
     * @param <T>
     * @param it
     *            an Iterator to wrap
     * @return an Iterable which returns the given iterator ONCE.
     */
    public static <T> Iterable<T> asIterable(final Iterator<T> it) {
        return new Iterable<T>() {

            boolean returned = false;

            public Iterator<T> iterator() {
                if (returned)
                    throw new IllegalStateException(
                        "Can only call iterator() once.");

                returned = true;

                return it;
            }
        };
    }

    public static <T> List<T> reverse(T[] original) {
        return reverse(Arrays.asList(original));
    }

    public static <T> List<T> reverse(List<T> original) {
        List<T> reversed = new ArrayList<T>(original);
        Collections.reverse(reversed);
        return reversed;
    }

    public static PacketFilter orFilter(final PacketFilter... filters) {

        return new PacketFilter() {

            public boolean accept(Packet packet) {

                for (PacketFilter filter : filters) {
                    if (filter.accept(packet)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
