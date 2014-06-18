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

package de.fu_berlin.inf.dpp.intellij.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageUtils {
    private static final Logger log = Logger.getLogger(MessageUtils.class);

    /**
     * Loads strings in to static variables from property file.
     *
     * @param propertyFile
     * @param clazz
     */
    public static void initializeMessages(String propertyFile, Class clazz) {
        try {
            log.info("Loading bundle [" + propertyFile + "]");

            ResourceBundle resourceBundle = ResourceBundle
                .getBundle(propertyFile, Locale.getDefault());

            for (Field f : clazz.getFields()) {
                String fieldName = f.getName();
                String fieldValue = resourceBundle.getString(fieldName);
                if (f.isAccessible() && f.getType().equals(String.class)) {
                    f.set(clazz, fieldValue);
                }
            }
        } catch (Exception e) {
            //it can not happen anyway!
        }

    }
}
