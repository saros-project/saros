/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import de.fu_berlin.inf.dpp.IActivityProvider;
import de.fu_berlin.inf.dpp.IActivitySequencer;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;

public class ActivitySequencer implements IActivitySequencer {
    
    private class TimedActivity {
        public IActivity activity;
        public int time;
    }
    
    private List<IActivity>         activities = new LinkedList<IActivity>();
    private List<IActivity>         flushed    = new LinkedList<IActivity>();

    private List<IActivityProvider> providers  = new LinkedList<IActivityProvider>();
    private List<TimedActivity>     queue      = new CopyOnWriteArrayList<TimedActivity>();
    private int                     time       = 0;
    private ITextSelection          lastSelection;
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public void exec(int time, IActivity activity) {
        TimedActivity timedActivity = new TimedActivity();
        timedActivity.activity = activity;
        timedActivity.time = time;
        
        queue.add(timedActivity);
        
        execQueue();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public void addProvider(IActivityProvider provider) {
        providers.add(provider);
        provider.addActivityListener(this);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public void removeProvider(IActivityProvider provider) {
        providers.remove(provider);
        provider.removeActivityListener(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public List<IActivity> flush() {
        List<IActivity> out = new ArrayList<IActivity>(activities.size());
        
        if (activities.size() > 0) {
            out.addAll(activities);
            activities.clear();
        }
        
        joinTextEdits(out);
        lastSelection = stripRedundantCursors(out, lastSelection);
        
        flushed.addAll(out);
        return out.size() > 0 ? out : null;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public List<IActivity> getLog() {
        return flushed;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivitySequencer
     */
    public int incTime(int amount) {
        int start = time;
        time += amount;
        return start;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityListener
     */
    public void activityCreated(IActivity activity) {
        activities.add(activity);
    }

    private void execQueue() {
        boolean changed = false;
        
        for (TimedActivity timedActivity : queue) {
            if (timedActivity.time <= time) { // HACK
                queue.remove(timedActivity);
                
                time++;
                exec(timedActivity);
                
                changed = true;
            }
        }
        
        if (changed)
            execQueue();
    }
    
    private void exec(TimedActivity timedActivity) {
        for (IActivityProvider executor : providers) {
            executor.exec(timedActivity.activity);
        }
    }

    /**
     * @param activities A list of activities that will be modified inline.
     */
    private static void joinTextEdits(List<IActivity> activities) {
        List<IActivity> result = new ArrayList<IActivity>(activities.size());
        
        TextEditActivity lastTextEdit = null;
        for (IActivity activity : activities) {
            if (activity instanceof TextEditActivity) {
                TextEditActivity textEdit = (TextEditActivity)activity;
                
                if (lastTextEdit != null && 
                    textEdit.offset == lastTextEdit.offset + lastTextEdit.text.length()) {
                    
                    result.remove(lastTextEdit);
                    textEdit = new TextEditActivity(lastTextEdit.offset,
                        lastTextEdit.text + textEdit.text, 
                        lastTextEdit.replace + textEdit.replace);
                }
                
                result.add(textEdit);
                lastTextEdit = textEdit;
                
            } else {
                result.add(activity);
                lastTextEdit = null;
            }
        }
        
        activities.clear();
        activities.addAll(result);
    }

    /**
     * @param activities a list of activities will be modified inline.
     * @param startSelection a selection that should be used as base or
     * <code>null</code>.
     * @return the last text selection.
     */
    private static ITextSelection stripRedundantCursors(List<IActivity> activities, 
        ITextSelection startSelection) {
        
        List<IActivity> result = new ArrayList<IActivity>(activities.size());
        
        ITextSelection lastSelection = startSelection;
        
        for (IActivity activity : activities) {
            if (activity instanceof CursorOffsetActivity) {
                CursorOffsetActivity cursorOffset = (CursorOffsetActivity)activity;
                
                if (lastSelection == null ||
                    cursorOffset.getOffset() != lastSelection.getOffset() || 
                    cursorOffset.getLength() != lastSelection.getLength()) {
                    
                    result.add(cursorOffset);
                }
                
                lastSelection = new TextSelection(cursorOffset.getOffset(), 
                    cursorOffset.getLength());
                
            } else if (activity instanceof TextEditActivity) {
                TextEditActivity textEdit = (TextEditActivity)activity;
                lastSelection = new TextSelection(textEdit.offset, 0);
                
                result.add(textEdit);
                
            } else {
                result.add(activity);
            }
        }
        
        activities.retainAll(result);
        return lastSelection;
    }
}
