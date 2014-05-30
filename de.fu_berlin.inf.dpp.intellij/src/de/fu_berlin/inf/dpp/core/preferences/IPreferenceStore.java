package de.fu_berlin.inf.dpp.core.preferences;

import de.fu_berlin.inf.dpp.core.exceptions.StorageException;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 * </p>
 * TODO This should either be a Saros/Core interface or more adapted to IntelliJ
 */
public interface IPreferenceStore {
    void flush() throws IOException;

    boolean getBoolean(String key, boolean value) throws StorageException;

    boolean getBoolean(String key);

    void putBoolean(String key, boolean value, boolean arg2)
        throws StorageException;

    void putByteArray(String key, byte[] value, boolean arg2)
        throws StorageException;

    byte[] getByteArray(String key, byte[] value) throws StorageException;

    int getInt(String kry);

    String getString(String key);

    void setValue(Object key, boolean value);

    void setValue(Object key, String value);

    String getDefaultString(String key);

}
