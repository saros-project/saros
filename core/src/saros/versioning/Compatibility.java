package saros.versioning;

import java.util.Comparator;

/** Enumeration to describe whether a local version is compatible with a remote one. */
public enum Compatibility {

  /** Versions are (probably) compatible */
  OK(0) {
    @Override
    public Compatibility invert() {
      return OK;
    }
  },
  /**
   * The local version is (probably) too old to work with the remote version.
   *
   * <p>The user should be told to upgrade
   */
  OLDER(1) {
    @Override
    public Compatibility invert() {
      return NEWER;
    }
  },
  /**
   * The local version is (probably) too new to work with the remote version.
   *
   * <p>The user should be told to tell the peer to update.
   */
  NEWER(2) {
    @Override
    public Compatibility invert() {
      return OLDER;
    }
  },

  /** The compatibility could not be determined. */
  UNKNOWN(3) {
    @Override
    public Compatibility invert() {
      return UNKNOWN;
    }
  },

  /**
   * At least one of the versions contains a qualifiers and the complete version string does not
   * match.
   */
  QUALIFIER_MISMATCH(4) {
    @Override
    public Compatibility invert() {
      return QUALIFIER_MISMATCH;
    }
  };

  private final int code;

  Compatibility(final int code) {
    this.code = code;
  }

  /**
   * @return <code>TOO_OLD</code> if the initial compatibility was <code>TOO_NEW</code>, <code>
   *     TOO_NEW</code> if the initial compatibility was <code>TOO_OLD</code>, <code>OK</code>
   *     otherwise
   */
  public abstract Compatibility invert();

  public int getCode() {
    return code;
  }

  public static Compatibility fromCode(int code) {
    for (Compatibility compatibility : Compatibility.values()) {
      if (compatibility.getCode() == code) return compatibility;
    }

    return UNKNOWN;
  }

  /**
   * Given a result from {@link Comparator#compare(Object, Object)} will return the associated
   * Compatibility object
   */
  public static Compatibility valueOf(int comparison) {
    switch (Integer.signum(comparison)) {
      case -1:
        return OLDER;
      case 0:
        return OK;
      case 1:
      default:
        return NEWER;
    }
  }
}
