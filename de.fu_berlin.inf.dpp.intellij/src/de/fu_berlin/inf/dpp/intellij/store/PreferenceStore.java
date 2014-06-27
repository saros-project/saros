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

package de.fu_berlin.inf.dpp.intellij.store;

import com.intellij.ide.util.PropertiesComponent;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;

/**
 * IntelliJ preference store implemented using {PropertiesComponent}.
 */
public class PreferenceStore implements IPreferenceStore {

    private static final String PROPERTY_PREFIX = "de.fu_berlin.inf.dpp.config.";

    private PropertiesComponent properties;

    /**
     * Creates a new PreferenceStore and initializes the PropertiesComponent.
     */
    public PreferenceStore() {
        properties = PropertiesComponent.getInstance();
    }

    @Override
    public int getInt(String key) {
        String value = properties.getValue(PROPERTY_PREFIX + key);
        if (value == null) {
            return DEFAULT_INT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_INT;
        }
    }

    @Override
    public boolean getBoolean(String key) {
        String value = properties.getValue(PROPERTY_PREFIX + key);
        return value == null ? DEFAULT_BOOLEAN : Boolean.valueOf(value);
    }

    @Override
    public String getString(String key) {
        return properties.getValue(PROPERTY_PREFIX + key, DEFAULT_STRING);
    }

    @Override
    public void setValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    @Override
    public void setValue(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    @Override
    public void setValue(String key, String value) {
        properties.setValue(PROPERTY_PREFIX + key, value);
    }
}
