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

package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import org.eclipse.swt.widgets.Display;

import java.util.concurrent.CountDownLatch;

/**
 * Implementation of a thread that creates the event dispatch loop for SWT.
 * This thread has to be started in order to able to use SWT components inside IntelliJ.
 */
class SwtThread extends Thread {

    private final CountDownLatch displayCreatedLatch;

    SwtThread(CountDownLatch displayCreatedLatch) {
        this.displayCreatedLatch = displayCreatedLatch;
    }

    @Override
    public void run() {
        // Execute the SWT event dispatch loop...
        Display display = Display
            .getDefault(); // creates new one if none present
        displayCreatedLatch.countDown();
        while (!display.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
