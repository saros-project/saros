/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;

import java.awt.Rectangle;

/**
 * Intellij editor ViewPort listener
 */
public class StoppableViewPortListener extends AbstractStoppableListener
    implements VisibleAreaListener {

    public StoppableViewPortListener(EditorManager manager) {
        super(manager);
    }

    /**
     * Calls {@link EditorManager#generateViewport(SPath, LineRange)}.
     *
     * @param event
     */
    @Override
    public void visibleAreaChanged(VisibleAreaEvent event) {
        if (!enabled) {
            return;

        }
        SPath path = editorManager.getEditorPool()
            .getFile(event.getEditor().getDocument());

        if (path != null) {
            editorManager.generateViewport(path, getLineRange(event));
        }

    }

    private LineRange getLineRange(VisibleAreaEvent event) {
        Rectangle rec = event.getEditor().getScrollingModel()
            .getVisibleAreaOnScrollingFinished();
        int lineHeight = event.getEditor().getLineHeight();

        return new LineRange((int) (rec.getMinY() / lineHeight),
            (int) (rec.getMaxY() / lineHeight));
    }
}
