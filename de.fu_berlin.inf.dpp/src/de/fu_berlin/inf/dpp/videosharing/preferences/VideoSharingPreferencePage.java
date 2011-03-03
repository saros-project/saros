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
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;

/**
 * Main preference page of {@link VideoSharing}.
 * 
 * @author s-lau
 */
@Component(module = "prefs")
public class VideoSharingPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    public VideoSharingPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Here you can set up basic settings for screensharing. Check the sub-preference pages at the left side to configure advanced settings.");
    }

    @Override
    protected void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 15;
        composite.setLayout(layout);
        composite
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addField(VideoSharingPreferenceHelper
            .getEncoderComboFieldEditor(composite));
        addField(VideoSharingPreferenceHelper
            .getResolutionComboFieldEditor(composite));
        addField(VideoSharingPreferenceHelper
            .getBandwidthComboFieldEditor(composite));
        addField(VideoSharingPreferenceHelper
            .getFollowMouseZoomFieldEditor(composite));
    }

    public void init(IWorkbench workbench) {
        // nothing to initialize
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        VideoSharingPreferenceHelper.checkXugglerInstallationOnPropertyChange(
            this, event);
    }

}
