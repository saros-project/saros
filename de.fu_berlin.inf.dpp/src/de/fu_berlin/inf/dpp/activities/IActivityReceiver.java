package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

public interface IActivityReceiver {

    boolean receive(ViewportActivity viewportActivity);

    boolean receive(TextSelectionActivity textSelectionActivity);

    boolean receive(TextEditActivity textEditActivity);

    boolean receive(RoleActivity roleActivity);

    boolean receive(FolderActivity folderActivity);

    boolean receive(FileActivity fileActivity);

    boolean receive(EditorActivity editorActivity);

    boolean receive(JupiterActivity jupiterActivity);
}
