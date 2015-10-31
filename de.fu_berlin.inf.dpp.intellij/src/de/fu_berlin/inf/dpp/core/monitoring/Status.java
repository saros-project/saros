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

package de.fu_berlin.inf.dpp.core.monitoring;

/**
 * This is a dummy implementation to ease the copy-paste-adapt process of
 * creating Saros/I out of Saros/E.
 * <p/>
 * TODO Check whether this actually necessary
 */
public class Status implements IStatus {
    public static final Status CANCEL_STATUS = new Status(0);
    public static final Status OK_STATUS = new Status(1);

    public Status() {
        // Do nothing
    }

    public Status(int status) {
        // Do nothing
    }

    public Status(int status, String msg, String title) {
        // Do nothing
    }

    public Status(int status, String msg, String title, Exception ex) {
        // Do nothing
    }
}
