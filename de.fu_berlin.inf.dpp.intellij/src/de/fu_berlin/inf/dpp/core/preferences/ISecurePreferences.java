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
public interface ISecurePreferences {
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] defValue) throws StorageException;

    byte[] getByteArray(String key) throws StorageException;

    boolean getBoolean(String key, boolean defValue) throws StorageException;

    void putBoolean(String key, boolean value, boolean arg2)
        throws StorageException;

    void putByteArray(String key, byte[] value, boolean arg2)
        throws StorageException;

    public String get(String key, String def) throws StorageException;

    String absolutePath();

    public double getDouble(String key, double def) throws StorageException;

    public float getFloat(String key, float def) throws StorageException;

    public int getInt(String key, int def) throws StorageException;

    public long getLong(String key, long def) throws StorageException;

    public void remove(String key);

    public boolean isEncrypted(String key) throws StorageException;

    public String name();

    void clear();
}
