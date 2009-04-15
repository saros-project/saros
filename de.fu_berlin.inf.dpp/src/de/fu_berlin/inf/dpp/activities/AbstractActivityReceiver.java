package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;

public class AbstractActivityReceiver implements IActivityReceiver {

    public boolean receive(ViewportActivity viewportActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(TextSelectionActivity textSelectionActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(TextEditActivity textEditActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(RoleActivity roleActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(FolderActivity folderActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(FileActivity fileActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(EditorActivity editorActivity) {
        // empty implementation
        return false;
    }

    public boolean receive(Request jupiterRequest) {
        // empty implementation
        return false;
    }
}
