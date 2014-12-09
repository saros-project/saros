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

package de.fu_berlin.inf.dpp.ui.model;

/**
 * Represent an entry in a contact list.
 */
public class Contact {
    private boolean isOnline;
    private boolean isHidden;
    private String displayName;

    /**
     * @param displayName the name of the contact as it should be displayed
     * @param isOnline boolean indicating online status
     */
    public Contact(String displayName, boolean isOnline) {
        this.displayName = displayName;
        this.isOnline = isOnline;
        isHidden = false;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public String getDisplayName() {
        return displayName;
    }
}
