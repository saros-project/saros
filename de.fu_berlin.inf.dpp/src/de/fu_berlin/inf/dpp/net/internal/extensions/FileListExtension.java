package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.util.Utils;

public class FileListExtension extends ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    private final List<byte[]> serializedFileLists;

    private transient List<FileList> deserializedFileLists;

    public FileListExtension(String sessionID, String negotiationID,
        FileList... fileLists) {
        super(sessionID, negotiationID);

        serializedFileLists = new ArrayList<byte[]>(fileLists.length);

        try {

            for (FileList list : fileLists)
                serializedFileLists.add(Utils.deflate(
                    list.toXML().getBytes("UTF-8"), null));

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                "corrupt JVM installation - UTF-8 charset is not supported", e);
        }
    }

    /**
     * Returns an unmodifiable list of the file lists.
     * 
     * @return
     */
    public synchronized List<FileList> getFileLists() {
        if (deserializedFileLists != null)
            return Collections.unmodifiableList(deserializedFileLists);

        deserializedFileLists = new ArrayList<FileList>(
            serializedFileLists.size());

        try {

            for (byte[] serializedFileList : serializedFileLists)
                deserializedFileLists.add(FileList.fromXML(new String(Utils
                    .inflate(serializedFileList, null), "UTF-8")));

        } catch (Exception e) {
            throw new RuntimeException("failed to deserialize the file lists",
                e);
        }

        return Collections.unmodifiableList(deserializedFileLists);
    }

    public static class Provider extends
        ProjectNegotiationExtension.Provider<FileListExtension> {

        private Provider() {
            super("fileList", FileListExtension.class);
        }
    }
}
