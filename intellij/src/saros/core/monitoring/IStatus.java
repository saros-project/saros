package saros.core.monitoring;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
// TODO This should either be a Saros/Core interface or more adapted to Intellij.
public interface IStatus {

  int OK = 0;

  int INFO = 1;

  int WARNING = 2;

  int ERROR = 4;

  int CANCEL = 8;
}
