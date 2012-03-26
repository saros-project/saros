/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2010
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
package de.fu_berlin.inf.dpp.videosharing.activities;

/**
 * Carries an exception to the buddy for information.
 * 
 * @author s-lau
 */
public class SessionErrorVideoActivity extends SessionVideoActivity {
    private static final long serialVersionUID = 449239759697079664L;

    protected Exception exception;

    public SessionErrorVideoActivity(Exception localException) {
        super(Type.SESSION_ERROR);
        this.exception = localException;
    }

    public Exception getException() {
        return exception;
    }

}
