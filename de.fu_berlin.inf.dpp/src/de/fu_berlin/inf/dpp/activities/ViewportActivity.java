package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.ILineRange;

public class ViewportActivity extends AbstractActivity {
    public final int topIndex;

    public final int bottomIndex;

    private IPath editor;

    public ViewportActivity(int topIndex, int bottomIndex, IPath editor) {

        assert topIndex <= bottomIndex : "Top == " + topIndex + ", Bottom == "
            + bottomIndex;

        this.topIndex = topIndex;
        this.bottomIndex = bottomIndex;
        this.editor = editor;
    }

    public ViewportActivity(ILineRange viewport, IPath editor2) {
        this(Math.max(0, viewport.getStartLine()), Math.max(0, viewport
            .getStartLine())
            + Math.max(0, viewport.getNumberOfLines()), editor2);
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
    public boolean equals(Object obj) {
        if (obj instanceof ViewportActivity) {
            ViewportActivity other = (ViewportActivity) obj;
            return (this.topIndex == other.topIndex)
                && (this.bottomIndex == other.bottomIndex)
                && ObjectUtils.equals(this.editor, other.editor);

        }
        return false;
    }

    @Override
    public String toString() {
        return "ViewportActivity(top:" + this.topIndex + ",bottom:"
            + this.bottomIndex + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {

        assert getEditor() != null;

        sb.append("<viewport ");
        sb.append("top=\"").append(getTopIndex()).append("\" ");
        sb.append("bottom=\"").append(getBottomIndex()).append("\" ");
        sb.append("editor=\"").append(getEditor().toPortableString()).append(
            "\"");
        sb.append("/>");
    }
}
