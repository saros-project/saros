/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
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
package de.fu_berlin.inf.dpp.videosharing.preferences;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * @author s-lau
 */
@Component(module = "prefs")
public class VideoPlayerPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    protected Composite parent;
    protected BooleanFieldEditor resampleField;
    protected FieldEditor keepAspectRatioField;

    public VideoPlayerPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Settings for displaying the video in the view");
    }

    @Override
    protected void createFieldEditors() {
        parent = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 15;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        resampleField = new BooleanFieldEditor(
            PreferenceConstants.PLAYER_RESAMPLE, "Resample input", parent);
        keepAspectRatioField = new BooleanFieldEditor(
            PreferenceConstants.PLAYER_KEEP_ASPECT_RATIO, "Keep aspect ratio",
            parent);
        keepAspectRatioField.setEnabled(getPreferenceStore().getBoolean(
            PreferenceConstants.PLAYER_RESAMPLE), parent);

        addField(resampleField);
        addField(keepAspectRatioField);
    }

    public void init(IWorkbench workbench) {
        // NOP

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(
                PreferenceConstants.PLAYER_RESAMPLE)) {
                if (event.getNewValue() instanceof Boolean) {
                    Boolean newValue = (Boolean) event.getNewValue();
                    keepAspectRatioField.setEnabled(newValue, parent);
                }
            }
        }
    }

}
