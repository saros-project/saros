/*
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
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
 */

package de.fu_berlin.inf.dpp.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Bind;

/**
 * This interface contains marker interfaces for binding components to specific keys.
 *
 * @see PicoContainer#getComponent(Object)
 * @see MutablePicoContainer#addComponent(Object, Object, org.picocontainer.Parameter...)
 * @see BindKey#bindKey(Class, Class)
 */
public interface IContextKeyBindings {
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public @interface IBBStreamService {
    // marker interface
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public @interface Socks5StreamService {
    // marker interface
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public @interface SarosVersion {
    // marker interface
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Bind
  public @interface PlatformVersion {
    // marker interface
  }
}
