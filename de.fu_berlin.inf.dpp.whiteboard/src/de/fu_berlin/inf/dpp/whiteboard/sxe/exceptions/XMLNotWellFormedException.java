package de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;

public class XMLNotWellFormedException extends RuntimeException {

  /** */
  private static final long serialVersionUID = 6788007030117793776L;

  private boolean conflict = false;
  private final SetRecord causingSet;

  public XMLNotWellFormedException(SetRecord causingSet) {
    this.causingSet = causingSet;
  }

  public XMLNotWellFormedException(SetRecord causingSet, boolean conflict) {
    this.causingSet = causingSet;
    this.conflict = conflict;
  }

  public boolean happedDueToAConflict() {
    return conflict;
  }

  public SetRecord getCausingSetRecord() {
    return causingSet;
  }
}
