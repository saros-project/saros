package de.fu_berlin.inf.dpp.activities;

public interface IActivityReceiver {

    boolean receive(ViewportActivity viewportActivity);

    boolean receive(TextSelectionActivity textSelectionActivity);

    boolean receive(TextEditActivity textEditActivity);

    boolean receive(RoleActivity roleActivity);

    boolean receive(FolderActivity folderActivity);

    boolean receive(FileActivity fileActivity);

    boolean receive(EditorActivity editorActivity);

}
