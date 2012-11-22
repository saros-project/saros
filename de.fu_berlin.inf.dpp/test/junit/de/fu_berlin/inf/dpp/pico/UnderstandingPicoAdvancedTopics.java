package de.fu_berlin.inf.dpp.pico;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.annotations.Bind;
import org.picocontainer.injectors.AdaptingInjection;

public class UnderstandingPicoAdvancedTopics {

    /*
     * Multiple components: Lets say you must add another component to the
     * context, but the component already exists.
     * 
     * E.G you need two different implementation of an interface or multiple
     * immutable objects like file references (java.io.File)
     * 
     * Solution use bindings !
     */

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public static @interface LogFile {
        // marker interface
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    @Bind
    public static @interface FileDirectory {
        // marker interface
    }

    public static class Logger {
        private File logFile;

        public Logger(@LogFile File logFile) {
            this.logFile = logFile;
        }

        public void log(String message) {
            System.out.println("Written message '" + message + "' to: "
                + logFile.getPath());
        }
    }

    public static class FileSystem {
        private File fileDirectory;

        public FileSystem(@FileDirectory File fileDirectory) {
            this.fileDirectory = fileDirectory;
        }

        public File getFile(String name) {
            return new File(fileDirectory, name);
        }
    }

    @Test
    public void testMultipleComponents() {
        MutablePicoContainer container = new PicoBuilder(
            new AdaptingInjection()).withCaching().build();

        container.addComponent(Logger.class);
        container.addComponent(FileSystem.class);

        container.addComponent(BindKey.bindKey(File.class, LogFile.class),
            new File("log"));

        container.addComponent(
            BindKey.bindKey(File.class, FileDirectory.class),
            new File("system"));

        Logger log = container.getComponent(Logger.class);
        FileSystem fileSystem = container.getComponent(FileSystem.class);

        log.log(fileSystem.getFile("textures.dat").getPath());
    }
}
