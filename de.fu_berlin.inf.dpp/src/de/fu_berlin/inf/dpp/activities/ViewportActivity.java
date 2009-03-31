package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import de.fu_berlin.inf.dpp.util.Util;

public class ViewportActivity extends AbstractActivity {
    public final int topIndex;

    public final int bottomIndex;

    private final IPath editor;

    public ViewportActivity(String source, int topIndex, int bottomIndex,
        IPath editor) {
        super(source);

        assert topIndex <= bottomIndex : "Top == " + topIndex + ", Bottom == "
            + bottomIndex;

        this.topIndex = topIndex;
        this.bottomIndex = bottomIndex;
        this.editor = editor;
    }

    public ViewportActivity(String source, ILineRange viewport, IPath editor2) {
        this(source, Math.max(0, viewport.getStartLine()), Math.max(0, viewport
            .getStartLine())
            + Math.max(0, viewport.getNumberOfLines()), editor2);
    }

    public ILineRange getLineRange() {
        return new LineRange(topIndex, bottomIndex - topIndex);
    }

    public int getBottomIndex() {
        return this.bottomIndex;
    }

    public int getTopIndex() {
        return this.topIndex;
    }

    public IPath getEditor() {
        return this.editor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + bottomIndex;
        result = prime * result + ((editor == null) ? 0 : editor.hashCode());
        result = prime * result + topIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ViewportActivity))
            return false;
        ViewportActivity other = (ViewportActivity) obj;
        if (bottomIndex != other.bottomIndex)
            return false;
        if (editor == null) {
            if (other.editor != null)
                return false;
        } else if (!editor.equals(other.editor))
            return false;
        if (topIndex != other.topIndex)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ViewportActivity(path:" + this.editor + ",range:("
            + this.topIndex + "," + this.bottomIndex + "))";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {

        assert getEditor() != null;

        sb.append("<viewport ");
        sourceToXML(sb);
        sb.append("top=\"").append(getTopIndex()).append("\" ");
        sb.append("bottom=\"").append(getBottomIndex()).append("\" ");
        sb.append("editor=\"").append(
            Util.urlEscape(getEditor().toPortableString())).append("\"");
        sb.append("/>");
    }
}
