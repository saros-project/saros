package de.fu_berlin.inf.dpp.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Thread Factory which assigns a given name + consecutive number to created
 * threads.
 */
public class NamedThreadFactory implements ThreadFactory {

    ThreadFactory defaultFactory = Executors.defaultThreadFactory();

    int count = 0;

    String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    public Thread newThread(Runnable r) {

        Thread result = defaultFactory.newThread(r);

        int i;
        synchronized (this) {
            i = count++;
        }
        result.setName(name + i);

        return result;
    }

}
