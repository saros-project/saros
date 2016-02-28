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

package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.jetbrains.annotations.NotNull;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * Component that is initalized when a project is loaded.
 * It initializes the logging, shortcuts and the {@link IntelliJSarosLifecycle} singleton.
 */
public class SarosComponent
    implements com.intellij.openapi.components.ProjectComponent {

    /**
     * This is the plugin ID that identifies the saros plugin in the IDEA
     * ecosystem.
     */
    public static final String PLUGIN_ID = "de.fu_berlin.inf.dpp.intellij";
    
    public SarosComponent(final Project project) {
        loadLoggers();

        Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
        keymap.addShortcut("ActivateSarosToolWindow", new KeyboardShortcut(
            KeyStroke.getKeyStroke(KeyEvent.VK_F11,
                java.awt.event.InputEvent.ALT_DOWN_MASK), null
        ));

        IntellijSarosLifecycle.getInstance(project).start();
    }
    

    public static boolean isSwtBrowserEnabled() {
        return Boolean.getBoolean("saros.swtbrowser");
    }

    private void loadLoggers() {
        final ClassLoader contextClassLoader = Thread.currentThread()
            .getContextClassLoader();

        try {
            // change the context class loader so Log4J will find
            // the SarosLogFileAppender
            Thread.currentThread()
                .setContextClassLoader(SarosComponent.class.getClassLoader());

            PropertyConfigurator.configure(SarosComponent.class.getClassLoader()
                .getResource("saros.log4j.properties"));
        } catch (RuntimeException e) {
            LogLog.error("initializing loggers failed", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void initComponent() {
        //NOP
    }

    @Override
    public void disposeComponent() {
        //NOP
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Saros";
    }

    @Override
    public void projectOpened() {
        //TODO: Update project
    }

    @Override
    public void projectClosed() {
        //TODO: Update project
    }
}
