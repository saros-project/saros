package de.fu_berlin.inf.dpp.synchronize;

/**
 * Implementers of this interface can be blocked by the StopManager. Being blocked means that they
 * don't generate any activities and don't generate local changes that can be realized by the user,
 * e.g. text changes.
 */
public interface Blockable {

  public void block();

  public void unblock();
}
