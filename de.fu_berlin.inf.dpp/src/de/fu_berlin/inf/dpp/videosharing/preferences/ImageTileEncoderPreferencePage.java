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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Codec;

/**
 * @author s-lau
 */
@Component(module = "prefs")
public class ImageTileEncoderPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    protected Group chooseEncoderGroup;

    public ImageTileEncoderPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Set up the image tile encoder.\nNote: Changing values here does not affect compression.");
        if (!getPreferenceStore().getString(PreferenceConstants.ENCODING_CODEC)
            .equals(Codec.IMAGE.name()))
            setMessage(
                "You should choose the Tile-Encoder on the encoder preference-page.",
                IMessageProvider.INFORMATION);
    }

    @Override
    protected void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 15;
        composite.setLayout(layout);
        composite
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // addField(new ComboFieldEditor(PreferenceConstants.IMAGE_TILE_CODEC,
        // "Image codec", ImageTileEncoder.getSupportedImageFormats(),
        // composite));
        addField(new ComboFieldEditor(PreferenceConstants.IMAGE_TILE_COLORS,
            "Colors", new String[][] { { "16", "16" },
                { "8 bit (256)", "256" }, { "16 bit (65K)", "65536" },
                { "24 bit (16M)", "16777216" } }, composite));
        // addField(new ScaleFieldEditor(PreferenceConstants.IMAGE_TILE_QUALITY,
        // "Quality (only jpg)",
        // composite, 1, 100, 1, 10));
        addField(new BooleanFieldEditor(PreferenceConstants.IMAGE_TILE_DITHER,
            "Dithering", composite));
        // addField(new BooleanFieldEditor(
        // PreferenceConstants.IMAGE_TILE_SERPENTINE, "Serpentine", composite));
    }

    public void init(IWorkbench workbench) {
        // NOP
    }
}
