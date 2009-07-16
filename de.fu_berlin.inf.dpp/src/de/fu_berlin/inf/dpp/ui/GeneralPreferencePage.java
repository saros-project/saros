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
package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * Contains the basic preferences for Saros.
 * 
 * @author rdjemili
 */
@Component(module = "prefs")
public class GeneralPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    @Inject
    Saros saros;

    public GeneralPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        Saros.reinject(this);

        setPreferenceStore(saros.getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 15;
        composite.setLayout(layout);
        composite
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createJabberPreferences(composite);
        createFollowModePreferences(composite);
        createMultiDriverPreferences(composite);

    }

    protected void createJabberPreferences(Composite composite) {
        Composite group = createGroup("Jabber settings", composite);

        addField(new StringFieldEditor(PreferenceConstants.SERVER, "Server:",
            group));

        addField(new StringFieldEditor(PreferenceConstants.USERNAME,
            "Username:", group));

        StringFieldEditor passwordField = new StringFieldEditor(
            PreferenceConstants.PASSWORD, "Password:", group);
        passwordField.getTextControl(group).setEchoChar('*');
        addField(passwordField);

        addField(new BooleanFieldEditor(PreferenceConstants.AUTO_CONNECT,
            "Automatically connect on startup.", group));
    }

    protected void createFollowModePreferences(Composite composite) {
        Composite group = createGroup("Follow Mode", composite);

        addField(new BooleanFieldEditor(PreferenceConstants.AUTO_FOLLOW_MODE,
            "Start in Follow Mode.", group));

        addField(new BooleanFieldEditor(
            PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER,
            "On role changes follow exclusive driver automatically.", group));
    }

    protected void createMultiDriverPreferences(Composite composite) {
        Composite group = createGroup("Multi Driver", composite);

        addField(new BooleanFieldEditor(PreferenceConstants.MULTI_DRIVER,
            "Enable multi driver support.", group));

        addField(new BooleanFieldEditor(
            PreferenceConstants.CONCURRENT_UNDO,
            "Enable concurrent undo (only local changes are undone, session restart necessary).",
            group));
    }

    /**
     * @return a composite containing a group in the given composite with the
     *         given text
     */
    protected Composite createGroup(String text, Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(text);
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite composite = new Composite(group, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        return composite;
    }

    /*
     * @see org.eclipse.ui.IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
        // nothing to initialize
    }
}