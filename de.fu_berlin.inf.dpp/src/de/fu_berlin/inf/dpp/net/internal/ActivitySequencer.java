/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.concurrent.ConcurrentManager;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;

/**
 * Implements {@link IActivitySequencer} and {@link IActivityManager}.
 * 
 * @author rdjemili
 */
public class ActivitySequencer implements IActivitySequencer, IActivityManager {
	// TODO separate into two classes!?

	private static final int UNDEFINED_TIME = -1;

	private static Logger log = Logger.getLogger(ActivitySequencer.class
			.getName());

	private List<IActivity> activities = new LinkedList<IActivity>();

	private List<IActivity> flushedLog = new LinkedList<IActivity>();

	private List<IActivityProvider> providers = new LinkedList<IActivityProvider>();

	private List<TimedActivity> queue = new CopyOnWriteArrayList<TimedActivity>();

	private List<TimedActivity> activityHistory = new LinkedList<TimedActivity>();

	private int timestamp = UNDEFINED_TIME;

	private ConcurrentManager concurrentManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.IActivityManager
	 */
	public void exec(IActivity activity) {
		try {
			/* TODO: 
			 * 1. Überprüfung, ob hierfür ein entsprechendes Dokument
			 * existiert.
			 * 2. */
			
//			// HOST SIDE
//			if (concurrentManager != null && concurrentManager.isHost()) {
//				/* concurrent control */
//				IActivity transformedActivity = concurrentManager
//						.exec(activity);
//
//				/* the transformed operation for this client side. */
//				for (IActivityProvider executor : providers) {
//					executor.exec(transformedActivity);
//				}
//			}
//			// CLIENT SIDE
//			else {
				for (IActivityProvider executor : providers) {
					executor.exec(activity);
				}
//			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while executing activity.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IActivitySequencer
	 */
	public void exec(TimedActivity timedActivity) {
		queue.add(timedActivity);
		execQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivitySequencer
	 */
	public void exec(List<TimedActivity> activities) {
		queue.addAll(activities);
		execQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityManager
	 */
	public List<IActivity> flush() {
		List<IActivity> out = new ArrayList<IActivity>(activities);
		activities.clear();
		out = optimize(out);
		flushedLog.addAll(out);
		return out.size() > 0 ? out : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IActivitySequencer
	 */
	public List<TimedActivity> flushTimed() {
		List<IActivity> activities = flush();

		if (activities == null)
			return null;

		if (timestamp == UNDEFINED_TIME)
			timestamp = 0;

		List<TimedActivity> timedActivities = new ArrayList<TimedActivity>();
		for (IActivity activity : activities) {
			timedActivities.add(new TimedActivity(activity, timestamp++));
		}

		return timedActivities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityManager
	 */
	public void addProvider(IActivityProvider provider) {
		providers.add(provider);
		provider.addActivityListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityManager
	 */
	public void removeProvider(IActivityProvider provider) {
		providers.remove(provider);
		provider.removeActivityListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivitySequencer
	 */
	public List<IActivity> getLog() {
		return flushedLog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityListener
	 */
	public void activityCreated(IActivity activity) {
		/*TODO: 1. an dieser stelle muss der entsprechende jupiter server
		 * beim client oder der komplexe concurrent server des host für
		 * das jeweilige dokument erzeugt werden.*/
		
//		/* sync with jupiter logic. */
//		IActivity ac = concurrentManager.activityCreated(activity);
//		/* put into outgoing queue and send to all. */
//		if(ac != null){
//			activities.add(ac);
//		}

		activities.add(activity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IActivitySequencer
	 */
	public int getTimestamp() {
		return timestamp;
	}

	public int getQueuedActivities() {
		return queue.size();
	}

	public List<TimedActivity> getActivityHistory() {
		return activityHistory;
	}

	/**
	 * Executes as much activities as possible from the current queue regarding
	 * to their individual time stamps.
	 */
	private void execQueue() {
		boolean executed;

		do {
			executed = false;

			for (TimedActivity timedActivity : queue) {
				if (timestamp == UNDEFINED_TIME)
					timestamp = timedActivity.getTimestamp();

				if (timedActivity.getTimestamp() <= timestamp) {
					queue.remove(timedActivity);

					timestamp++;
					exec(timedActivity.getActivity());
					executed = true;
				}
			}

		} while (executed);

	}

	// TODO extract this into the activities themselves
	private List<IActivity> optimize(List<IActivity> activities) {
		List<IActivity> result = new ArrayList<IActivity>(activities.size());

		ITextSelection selection = null;
		String source = null;

		for (IActivity activity : activities) {
			source = null;
			if (activity instanceof TextEditActivity) {
				TextEditActivity textEdit = (TextEditActivity) activity;

				textEdit = joinTextEdits(result, textEdit);

				selection = new TextSelection(textEdit.offset
						+ textEdit.text.length(), 0);
				source = textEdit.getSource();
				result.add(textEdit);

			} else if (activity instanceof TextSelectionActivity) {
				TextSelectionActivity textSelection = (TextSelectionActivity) activity;

				selection = new TextSelection(textSelection.getOffset(),
						textSelection.getLength());
				source = textSelection.getSource();

			} else {
				selection = addSelection(result, selection, null);
				result.add(activity);
			}

			selection = addSelection(result, selection, source);
		}

		return result;
	}

	private TextEditActivity joinTextEdits(List<IActivity> result,
			TextEditActivity textEdit) {
		if (result.size() == 0)
			return textEdit;

		IActivity lastActivity = result.get(result.size() - 1);
		if (lastActivity instanceof TextEditActivity) {
			TextEditActivity lastTextEdit = (TextEditActivity) lastActivity;

			if ((lastTextEdit.getSource() == null || lastTextEdit.getSource()
					.equals(textEdit.getSource()))
					&& textEdit.offset == lastTextEdit.offset
							+ lastTextEdit.text.length()) {
				result.remove(lastTextEdit);
				textEdit = new TextEditActivity(lastTextEdit.offset,
						lastTextEdit.text + textEdit.text, lastTextEdit.replace
								+ textEdit.replace);
				textEdit.setSource(lastTextEdit.getSource());
				textEdit.setEditor(lastTextEdit.getEditor());
			}
		}

		return textEdit;
	}

	private ITextSelection addSelection(List<IActivity> result,
			ITextSelection selection, String source) {
		if (selection == null)
			return null;

		if (result.size() > 0) {
			IActivity lastActivity = result.get(result.size() - 1);
			if (lastActivity instanceof TextEditActivity) {
				TextEditActivity lastTextEdit = (TextEditActivity) lastActivity;

				if (selection.getOffset() == lastTextEdit.offset
						+ lastTextEdit.text.length()
						&& selection.getLength() == 0) {

					return selection;
				}
			}
		}

		TextSelectionActivity newSel = new TextSelectionActivity(selection
				.getOffset(), selection.getLength());
		newSel.setSource(source);
		result.add(newSel);

		selection = null;
		return selection;
	}

	public void initConcurrentManager(de.fu_berlin.inf.dpp.concurrent.ConcurrentManager.Side side, de.fu_berlin.inf.dpp.User host, JID myJID) {
		concurrentManager = new ConcurrentDocumentManager(side,host,myJID);
	}

	public ConcurrentManager getConcurrentManager() {
		return concurrentManager;
	}
}
