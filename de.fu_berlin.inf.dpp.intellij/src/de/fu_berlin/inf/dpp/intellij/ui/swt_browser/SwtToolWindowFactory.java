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

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;

import java.util.concurrent.CountDownLatch;

/**
 * This class is an implementation of a tool window factory and creates a tool window
 * containing a SWT browser. With the creation of the tool window the SWT event
 * thread is started.
 */
public class SwtToolWindowFactory implements ToolWindowFactory {

    //This is static as there is no documented guarantee that only one instance
    // of this class will be created
    private static volatile boolean swtInitialized;

    @Override
    public void createToolWindowContent(Project project,
        ToolWindow toolWindow) {
        // Prevent the SWT initialization from being executed twice
        synchronized (SwtToolWindowFactory.class) {
            if (!swtInitialized) {
                SwtLibLoader.loadSwtLib();
                startSwtEventThread();
                swtInitialized = true;
            }
        }

        SwtBrowserPanel swtBrowserPanel = new SwtBrowserPanel();
        Content content = toolWindow.getContentManager().getFactory()
            .createContent(swtBrowserPanel, PluginManager
                .getPlugin(PluginId.getId("de.fu_berlin.inf.dpp.intellij"))
                .getName(), false);
        toolWindow.getContentManager().addContent(content);
        swtBrowserPanel.initialize();
    }

    private void startSwtEventThread() {
        CountDownLatch displayCreatedLatch = new CountDownLatch(1);
        SwtThread swtThread = new SwtThread(displayCreatedLatch);
        //TODO OS detection should be externalized, maybe in the core
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            MacExecutor.run(swtThread);
        } else {
            swtThread.start();
        }
        try {
            displayCreatedLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}