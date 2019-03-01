package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * This observable can be used to check whether there is currently a file replacement activity in
 * progress by the ConsistencyWatchdog (in this case isReplacementInProgress() returns true).
 *
 * <p>Internally this class uses reference counting, so you can call startReplacement() repeatedly
 * and it will return true until a matching number of calls to replacementDone() has been made.
 */
@Component(module = "observables")
public class FileReplacementInProgressObservable {

  int numberOfFileReplacementsInProgress = 0;

  public synchronized boolean isReplacementInProgress() {
    return numberOfFileReplacementsInProgress > 0;
  }

  public synchronized void startReplacement() {
    numberOfFileReplacementsInProgress++;
  }

  public synchronized void replacementDone() {
    numberOfFileReplacementsInProgress--;
  }
}
