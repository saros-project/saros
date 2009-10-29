package de.fu_berlin.inf.dpp.activities.serializable;

/**
 * A Receiver is an interface for handling multiple dispatch in Java.
 * 
 * In our case we want to call one of the specialized receive methods in
 * IActivityDataObjectReceiver for a given IActivityDataObject.
 * 
 * For instance, if an IActivityDataObject is a TextSelectionActivityDataObject,
 * we want the method {@link #receive(TextSelectionActivityDataObject)} to be
 * called.
 * 
 */
public interface IActivityDataObjectReceiver {

    void receive(ViewportActivityDataObject viewportActivityDataObject);

    void receive(TextSelectionActivityDataObject textSelectionActivityDataObject);

    void receive(TextEditActivityDataObject textEditActivityDataObject);

    void receive(RoleActivityDataObject roleActivityDataObject);

    void receive(FolderActivityDataObject folderActivityDataObject);

    void receive(FileActivityDataObject fileActivityDataObject);

    void receive(EditorActivityDataObject editorActivityDataObject);

    void receive(JupiterActivityDataObject jupiterActivity);

    void receive(StopActivityDataObject stopActivityDataObject);

    void receive(ChecksumActivityDataObject checksumActivityDataObject);

    void receive(PingPongActivityDataObject pingPongActivityDataObject);
}
