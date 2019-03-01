/**
 * All events that occur in a shared project session need to be replayed on the computers of all
 * participants (events like opening/closing file, making a text edit, etc.). Activities are objects
 * which contain information about specific events that originate on one computer and are
 * transmitted to other participants. Thus, in a Saros session, peers are continually exchanging
 * activities invisibly.
 *
 * <p>Each activity should implement the {@link saros.activities.IActivity} interface.
 */
package saros.activities;
