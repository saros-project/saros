package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;

import java.io.File;

public class IntelliJFileResourceAttributesImpl implements IResourceAttributes {

    private File file;

    public IntelliJFileResourceAttributesImpl(File file) {
        this.file = file;
    }

    @Override
    public boolean isReadOnly() {
        return this.file.canRead();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.file.setReadable(true);
        this.file.setWritable(!readOnly);
    }
}
