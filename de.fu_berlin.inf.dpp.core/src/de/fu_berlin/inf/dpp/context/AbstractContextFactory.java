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

/**
 * Abstract base class that only offers syntactic sugar for handling component creation.
 *
 * @author srossbach
 */
public abstract class AbstractContextFactory implements IContextFactory {

  public static class Component {
    private Class<?> clazz;
    private Object instance;
    private Object bindKey;

    private Component(Object bindKey, Class<?> clazz, Object instance) {
      this.bindKey = bindKey;
      this.clazz = clazz;
      this.instance = instance;
    }

    public static Component create(Object bindKey, Class<?> clazz) {
      return new Component(bindKey, clazz, null);
    }

    public static Component create(Class<?> clazz) {
      return new Component(clazz, clazz, null);
    }

    public static <T> Component create(Class<T> clazz, T instance) {
      return new Component(clazz, clazz, instance);
    }

    public Object getBindKey() {
      return bindKey;
    }

    public Object getImplementation() {
      return instance != null ? instance : clazz;
    }
  }
}
