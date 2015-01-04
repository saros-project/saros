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

/**
 * IntelliJ preference store implemented using {@link PropertiesComponent}.
 */
public class PreferenceStore {

    private static final String PROPERTY_PREFIX = "de.fu_berlin.inf.dpp.config.";

    private PropertiesComponent properties;

    /**
     * The default-default value for boolean preferences (<code>false</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = false;

    /**
     * The default-default value for int preferences (<code>0</code>).
     */
    public static final int DEFAULT_INT = 0;

    /**
     * The default-default value for String preferences (<code>""</code>).
     */
    public static final String DEFAULT_STRING = "";

    /**
     * Creates a new PreferenceStore and initializes the PropertiesComponent.
     */
    public PreferenceStore() {
        properties = PropertiesComponent.getInstance();
    }

    /**
     * Returns the current value of the named preference as a Integer. If there
     * is no preference or the value can not be converted to a integer, it
     * returns <code>0</code>.
     *
     * @param key of the named preference
     * @return the current integer value
     */
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

    /**
     * Returns the current value of the named preference as a Boolean. If there
     * is no preference or the value can not be converted to a boolean, it
     * returns <code>false</code>.
     *
     * @param key of the named preference
     * @return the current boolean value
     */
    public boolean getBoolean(String key) {
        String value = properties.getValue(PROPERTY_PREFIX + key);
        return value == null ? DEFAULT_BOOLEAN : Boolean.valueOf(value);
    }

    /**
     * Returns the current value of the named preference as a String. If there
     * is no preference or the value can not be converted to a String, it
     * returns <code>""(empty String)</code>.
     *
     * @param key of the named preference
     * @return the default String value
     */
    public String getString(String key) {
        return properties.getValue(PROPERTY_PREFIX + key, DEFAULT_STRING);
    }

    /**
     * Sets the current value to an Integer valued preference for the given
     * name. If the new value differ from the old value, a preferenceChangeEvent
     * will be reported.
     *
     * @param key   of the named preference
     * @param value new current value
     */
    public void setValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    /**
     * Sets the current value to an Boolean valued preference for the given
     * name. If the new value differ from the old value, a preferenceChangeEvent
     * will be reported.
     *
     * @param key   of the named preference
     * @param value new current value
     */
    public void setValue(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    /**
     * Sets the current value to an String valued preference for the given name.
     * If the new value differ from the old value, a preferenceChangeEvent will
     * be reported.
     *
     * @param key   of the named preference
     * @param value new current value, can not be <code>null</code>
     */
    public void setValue(String key, String value) {
        properties.setValue(PROPERTY_PREFIX + key, value);
    }
}
