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
package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.MessagingManager;
import de.fu_berlin.inf.dpp.Saros;

/**
 * TODO Very kludgy window for chat messages. Needs some work
 * 
 * @author rdjemili
 * 
 */
public class MessagingWindow extends ApplicationWindow {
	private MessagingManager.SessionProvider session;

	private TextViewer historyViewer;

	private StyledText textInput;

	public MessagingWindow(MessagingManager.SessionProvider session) {
		super(null); // top-level window

		this.session = session;
	}

	@Override
	protected Control createContents(Composite parent) {
		// main composite
		Composite composite = new Composite(parent, SWT.None);
		FillLayout fillLayout = new FillLayout();
		// fillLayout.marginHeight = 5;
		// fillLayout.marginWidth = 5;
		composite.setLayout(fillLayout);

		SashForm form = new SashForm(composite, SWT.VERTICAL);

		historyViewer = new TextViewer(form, SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
		historyViewer.setDocument(new Document());

		textInput = new StyledText(form, SWT.MULTI | SWT.BORDER);
		textInput.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					e.doit = false;
					sendInput();
				}
			}

			public void keyReleased(KeyEvent e) {
				// ignore
			}
		});
		textInput.setFocus();

		form.setWeights(new int[] { 5, 1 });

		// fill textViewer
		for (MessagingManager.ChatLine chatLine : session.getHistory()) {
			addChatLine(chatLine);
		}

		return composite;
	}

	@Override
	protected void configureShell(Shell shell) {
    	super.configureShell(shell);

		shell.setText("Talking to "+session.getName());
		shell.setSize(500, 400);
		shell.setImage(SarosUI.getImage("icons/comment.png"));
	}

	private void sendInput() {
		String msg = textInput.getText().trim();
		textInput.setText("");

		session.sendMessage(msg);
	}

	/**
	 * Add a chat line to the text viewer.
	 */
	public void addChatLine(MessagingManager.ChatLine line) {
		String sender = line.sender;
		String text = line.text;

		IDocument document = historyViewer.getDocument();
		int start = document.getLength();

		// build and append new chat line string
		StringBuffer newLine = new StringBuffer();
		if (start > 0) {
			newLine.append('\n');
			start++;
		}
		newLine.append(sender);
		newLine.append(": ");
		newLine.append(text);

		try {
			document.replace(document.getLength(), 0, newLine.toString());
		} catch (BadLocationException e) {
			Saros.log("Could not update chat window", e);
		}

		// Show sender in bold
		StyledText styledText = historyViewer.getTextWidget();
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = sender.length();
		styleRange.fontStyle = SWT.BOLD;
		styledText.setStyleRange(styleRange);
	}
}