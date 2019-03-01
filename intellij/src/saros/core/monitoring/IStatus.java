package saros.core.monitoring;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart. TODO This
 * should either be a Saros/Core interface or more adapted to IntelliJ
 */
public interface IStatus {

  public static final int OK = 0;

  public static final int INFO = 1;

  public static final int WARNING = 2;

  public static final int ERROR = 4;

  public static final int CANCEL = 8;
}
