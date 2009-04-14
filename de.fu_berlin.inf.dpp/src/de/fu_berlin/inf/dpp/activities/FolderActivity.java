package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("folder")
public class FolderActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed
    }

    @XStreamAsAttribute
    private final Type type;

    @XStreamAsAttribute
    private final IPath path;

    public FolderActivity(String source, Type type, IPath path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    public IPath getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "FolderActivity(type:" + this.type + ",path:" + this.path + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {
        sb.append(xstream.toXML(this));
    }
}
