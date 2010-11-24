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

import java.util.Arrays;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Codec;
import de.fu_berlin.inf.dpp.videosharing.encode.XugglerEncoder;

/**
 * Static functions and widely used fields for video-sharing's preference pages.
 * 
 * @author s-lau
 */
public class VideoSharingPreferenceHelper {

    public static final String XUGGLER_NOT_INSTALLED_MESSAGE = "Xuggler is not installed!";

    public static final String[][] RESOLUTIONS = new String[][] {
        { " 160x120  (QVGA,  4:3)", "160x120" },
        { " 320x200  (CGA,  16:10)", "320x200" },
        { " 320x240  (QVGA,  4:3)", "320x240" },
        { " 640x360  (QHD,  16:9)", "640x360" },
        { " 640x480  (VGA,   4:3)", "640x480" },
        { " 800x600  (SVGA,  4:3)", "800x600" },
        { " 960x540  (QHD,  16:9)", "960x540" },
        { "1024x768  (XGA,   4:3)", "1024x768" },
        { "1280x720  (720p, 16:9)", "1280x720" },
        { "1280x800  (WXGA, 16:10)", "1280x800" },
        { "custom (set it on the encoder preference page)", "" } };

    public static final String[][] BANDWIDTHS = new String[][] {
        { "64   kbit/s", "65536" }, { "128  kbit/s", "131072" },
        { "256  kbit/s", "262144" }, { "512  kbit/s", "524288" },
        { "768  kbit/s", "786432" }, { "1024 kbit/s", "1048576" },
        { "2048 kbit/s", "2097152" },
        { "custom (set it on the encoder preference page)", "" } };

    public static final String[][] ZOOM_LEVELS = new String[][] {
        { "1x (100%, original size)", "100" }, { "1.5x", "150" },
        { "2x", "200" }, { "2.5x", "250" }, { "3x", "300" }, { "3.5x", "350" },
        { "4x (25%)", "400" },
        { "custom (set resolution on the desktop preference page)", "" } };

    public static final ComboFieldEditor getEncoderComboFieldEditor(
        Composite parent) {
        return new EncoderComboFieldEditor(PreferenceConstants.ENCODING_CODEC,
            "Encoder", VideoSharing.Codec.getNamesAndValues(), parent);
    }

    public static ComboFieldEditor getResolutionComboFieldEditor(
        final Composite parent) {
        return new ResolutionComboFieldEditor(
            PreferenceConstants.ENCODING_VIDEO_RESOLUTION, "Video resolution",
            RESOLUTIONS, parent);
    }

    public static ComboFieldEditor getBandwidthComboFieldEditor(
        final Composite parent) {
        return new BandwidthComboFieldEditor(
            PreferenceConstants.ENCODING_MAX_BITRATE_COMBO, "Bandwidth",
            BANDWIDTHS, parent);
    }

    public static ComboFieldEditor getFollowMouseZoomFieldEditor(
        Composite parent) {
        return new FollowMouseZoomComboFieldEditor(
            PreferenceConstants.SCREEN_MOUSE_AREA_QUALITY,
            "Captured area in follow mouse mode", ZOOM_LEVELS, parent);

    }

    public static void checkXugglerInstallationOnPropertyChange(
        FieldEditorPreferencePage page, PropertyChangeEvent event) {
        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(
                PreferenceConstants.ENCODING_CODEC)) {
                if (event.getNewValue() instanceof String) {
                    String newValue = (String) event.getNewValue();
                    if (newValue.equals(Codec.XUGGLER.name())) {
                        checkXugglerInstallation(page);
                    } else {
                        page.setErrorMessage(null);
                    }
                }
            }
        }
    }

    public static void checkXugglerInstallation(FieldEditorPreferencePage page) {
        if (!XugglerEncoder.isInstalled()) {
            page.setErrorMessage(XUGGLER_NOT_INSTALLED_MESSAGE);
        } else {
            page.setErrorMessage(null);
        }
    }

    private static final class EncoderComboFieldEditor extends ComboFieldEditor {
        private EncoderComboFieldEditor(String name, String labelText,
            String[][] entryNamesAndValues, Composite parent) {
            super(name, labelText, entryNamesAndValues, parent);
        }

        @Override
        protected void doLoad() {
            super.doLoad();

            if (Codec.XUGGLER.name().equals(
                getPreferenceStore().getString(
                    PreferenceConstants.ENCODING_CODEC))
                && !XugglerEncoder.isInstalled())
                getPage().setErrorMessage(XUGGLER_NOT_INSTALLED_MESSAGE);
        }
    }

    private static final class FollowMouseZoomComboFieldEditor extends
        ComboFieldEditor {
        private FollowMouseZoomComboFieldEditor(String name, String labelText,
            String[][] entryNamesAndValues, Composite parent) {
            super(name, labelText, entryNamesAndValues, parent);
        }

        @Override
        protected void doLoad() {
            super.doLoad();

            String rawZoom = getPreferenceStore().getString(
                PreferenceConstants.SCREEN_MOUSE_AREA_QUALITY);

            if (rawZoom == null || rawZoom.equals(""))
                return;

            int zoom = Integer.parseInt(rawZoom);

            if (!Arrays.equals(getCustomResolution(),
                calculateResolutionForZoom(zoom)))
                getPreferenceStore().setValue(getPreferenceName(), "");
        }

        @Override
        protected void doStore() {
            super.doStore();

            String rawZoom = getPreferenceStore().getString(
                PreferenceConstants.SCREEN_MOUSE_AREA_QUALITY);
            if (rawZoom == null || rawZoom.equals(""))
                return;

            int zoom = Integer.parseInt(rawZoom);
            int[] newRes = calculateResolutionForZoom(zoom);

            getPreferenceStore().setValue(
                PreferenceConstants.SCREEN_MOUSE_AREA_WIDTH, newRes[0]);
            getPreferenceStore().setValue(
                PreferenceConstants.SCREEN_MOUSE_AREA_HEIGHT, newRes[1]);

        }

        private int[] calculateResolutionForZoom(int zoom) {
            int width = getPreferenceStore().getInt(
                PreferenceConstants.ENCODING_VIDEO_WIDTH);
            int height = getPreferenceStore().getInt(
                PreferenceConstants.ENCODING_VIDEO_HEIGHT);

            float scaleFactor = (float) zoom / 100;

            return new int[] { (int) (scaleFactor * width),
                (int) (scaleFactor * height) };
        }

        /**
         * @return stored resolution in this field (0: width, 1: height)
         */
        private int[] getCustomResolution() {
            return new int[] {
                getPreferenceStore().getInt(
                    PreferenceConstants.SCREEN_MOUSE_AREA_WIDTH),
                getPreferenceStore().getInt(
                    PreferenceConstants.SCREEN_MOUSE_AREA_HEIGHT) };
        }
    }

    private static final class BandwidthComboFieldEditor extends
        ComboFieldEditor {
        private BandwidthComboFieldEditor(String name, String labelText,
            String[][] entryNamesAndValues, Composite parent) {
            super(name, labelText, entryNamesAndValues, parent);
        }

        @Override
        protected void doLoad() {
            int maxBRVal = getPreferenceStore().getInt(
                PreferenceConstants.ENCODING_MAX_BITRATE);
            int maxBRValCombo = getPreferenceStore()
                .getInt(getPreferenceName());

            if (maxBRVal != maxBRValCombo)
                getPreferenceStore().setValue(getPreferenceName(), "");

            super.doLoad();
        }

        @Override
        protected void doStore() {
            super.doStore();

            String maxBRValComboString = getPreferenceStore().getString(
                getPreferenceName());

            int maxBRValCombo;

            try {
                maxBRValCombo = Integer.parseInt(maxBRValComboString);
            } catch (NumberFormatException e) {
                maxBRValCombo = 0;
                return;
            }

            getPreferenceStore().setValue(
                PreferenceConstants.ENCODING_MAX_BITRATE, maxBRValCombo);
        }
    }

    private static final class ResolutionComboFieldEditor extends
        ComboFieldEditor {
        private ResolutionComboFieldEditor(String name, String labelText,
            String[][] entryNamesAndValues, Composite parent) {
            super(name, labelText, entryNamesAndValues, parent);
        }

        @Override
        protected void doLoad() {
            int[] res = getStoredResolution();
            if (res != null) {
                int width = getPreferenceStore().getInt(
                    PreferenceConstants.ENCODING_VIDEO_WIDTH);
                int height = getPreferenceStore().getInt(
                    PreferenceConstants.ENCODING_VIDEO_HEIGHT);
                if (res[0] != width || res[1] != height)
                    getPreferenceStore().setValue(getPreferenceName(), "");
            }
            super.doLoad();
        }

        @Override
        protected void doStore() {
            super.doStore();

            int[] res = getStoredResolution();
            if (res == null)
                return;

            getPreferenceStore().setValue(
                PreferenceConstants.ENCODING_VIDEO_WIDTH, res[0]);
            getPreferenceStore().setValue(
                PreferenceConstants.ENCODING_VIDEO_HEIGHT, res[1]);
        }

        @Override
        protected void fireValueChanged(String property, Object oldValue,
            Object newValue) {
            super.fireValueChanged(property, oldValue, newValue);
        }

        /**
         * @return stored resolution in this field (0: width, 1: height) or
         *         <code>null</code> when none set
         */
        private int[] getStoredResolution() {
            String storedVal = getPreferenceStore().getString(
                getPreferenceName());

            if (storedVal.equals(""))
                return null;

            String[] res = storedVal.split("x");

            return new int[] { Integer.parseInt(res[0]),
                Integer.parseInt(res[1]) };
        }
    }

}
