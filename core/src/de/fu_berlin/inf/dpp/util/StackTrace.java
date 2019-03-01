package de.fu_berlin.inf.dpp.util;

public class StackTrace extends RuntimeException {

  private static final long serialVersionUID = 7255007872463969041L;

  @Override
  public String toString() {
    return "StackTrace:";
  }
}
