/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package saros.activities;

import saros.session.User;

/**
 * An interface for all things that occur in a shared project session such as editing a file,
 * opening or closing editors, changing permissions, etc.
 *
 * <p>All {@link IActivity}s should be implemented using the value pattern, i.e. created {@link
 * IActivity} should be immutable.
 *
 * <p>{@link IActivity} are supposed to be "smart" and know about users and session.
 *
 * @valueObject All IActivity subclasses should be Value Objects, i.e. they should be immutable
 */
public interface IActivity {

  /**
   * @JTourBusStop 1, Creating a new Activity type, The IActivity interface:
   *
   * <p>This tour explains what you need to consider when you want to create a new Activity type.
   *
   * <p>IActivity is the base interface for all activity implementations. The only common attribute
   * of all IActivity instances is the source, that is the session participant who did "something"
   * and therefore caused this activity in the first place.
   *
   * <p>So create a new class in the "activities" package with the suffix "Activity", and continue
   * with the next stop.
   */
  /**
   * @JTourBusStop 3, Some Basics:
   *
   * <p>When a session has begun, messages are passed between all participants to keep the session
   * synchronized. The messages are known as Activities and come in different types. Each type
   * inherits from this interface and implements its specific behavior.
   *
   * <p>Handling of Activities is done using the Inversion of Control pattern.
   */

  /**
   * Returns the user who caused this activity.
   *
   * @return not <code>null</code>
   */
  public User getSource();

  /**
   * Any implementation of IActivity needs to override this method in the following way:
   *
   * <pre>
   * &#064;Override
   * <b>public void</b> dispatch(IActivityReceiver receiver) {
   *     receiver.receive(<b>this</b>);
   * }
   * </pre>
   *
   * This way, the matching {@code receive()} method of the given {@code receiver} will be called,
   * which effectively provides the {@code receiver} with a type-safe activity object (type-safety
   * is also the reason why this method cannot be inherited from a common super class, although the
   * characters of the method body are always the same).
   */
  public void dispatch(IActivityReceiver receiver);

  /**
   * Since deserialization bypasses the class's constructor, this method is used to ensure only
   * valid objects are given to the business logic.
   *
   * <p>In principle, this method should return <code>false</code> for every object configuration
   * that would lead to an {@link IllegalArgumentException} if its constructor was used. However,
   * since this method is only expected to cover deserialization errors, it is sufficient for
   * implementations to only check for (unwanted) <code>null</code> values of fields of which the
   * conversions rely on external states (such as the Saros session), i.e. fields of type {@link
   * User} and {@link SPath}. The latter are the cases in which implementations <i>must</i> return
   * <code>false</code>; they are <i>allowed</i> to return <code>false</code> for all configurations
   * that would throw an {@link IllegalArgumentException} if the constructor was used.
   *
   * <p>An alternative solution for this could make use of the <a href=
   * "http://docs.oracle.com/javase/6/docs/api/java/io/Serializable.html"> <code>readResolve()
   * </code></a> method, which could return <code>null</code> on invalid objects.
   *
   * @return <code>true</code> if this object's state indicates a clean deserialization; always
   *     <code>true</code> for locally created objects
   */
  public boolean isValid();
}
