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
package de.fu_berlin.inf.dpp.exceptions;

/**
 * This is the superclass of all {@link Exception}s related to streaming. Should
 * be thrown when none of subclassed {@link Exception}s match.
 */
public class StreamException extends Exception {
    private static final long serialVersionUID = 1356096192698541354L;

    public StreamException() {
        super();
    }

    public StreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamException(String message) {
        super(message);
    }

    public StreamException(Throwable cause) {
        super(cause);
    }

}