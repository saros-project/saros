package de.fu_berlin.inf.dpp.intellij.editor;

/**
 * Abstract IntelliJ event listener.
 */
public abstract class AbstractStoppableListener {

    protected EditorManager editorManager;
    protected boolean enabled = true;

    public AbstractStoppableListener(EditorManager manager) {
        this.editorManager = manager;
    }

    /**
     * Enables or disables the forwarding of text changes. Default is enabled.
     *
     * @param enabled <code>true</code> to forward text changes, <code>false</code>
     *                otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
