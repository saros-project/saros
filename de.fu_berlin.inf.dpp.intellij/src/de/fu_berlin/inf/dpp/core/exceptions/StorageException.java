package de.fu_berlin.inf.dpp.core.exceptions;

/**
 * This is a dummy implementation to ease the copy-paste-adapt process of
 * creating Saros/I out of Saros/E.
 * <p/>
 * TODO This class might be unnecessary and its usages (if any) are probably
 * easy to replace.
 */
public class StorageException extends Exception {

    public StorageException() {
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
