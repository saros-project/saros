package saros.versioning;

import static saros.versioning.Compatibility.INCOMPATIBLE_IMPLEMENTATIONS;
import static saros.versioning.Compatibility.NEWER;
import static saros.versioning.Compatibility.OK;
import static saros.versioning.Compatibility.OLDER;
import static saros.versioning.Compatibility.QUALIFIER_MISMATCH;
import static saros.versioning.Compatibility.UNKNOWN;

import java.util.Objects;
import java.util.StringTokenizer;

public class Version {

  /** Unique version instance representing an invalid version. */
  public static final Version INVALID = new Version("invalid", 0, 0, 0, "invalid");

  static final String IMPLEMENTATION_SEPARATOR = "/";

  private static final String VERSION_SEPARATOR = ".";

  /**
   * The identifier of the specific Saros implementation (e.g. "<code>E</code>" for Saros/E or "
   * <code>I</code>" for Saros/I).
   */
  private final String implementation;

  private final int major;

  private final int minor;

  private final int micro;

  private final String qualifier;

  private final String asString;

  private Version(
      final String implementation,
      final int major,
      final int minor,
      final int micro,
      final String qualifier) {

    Objects.requireNonNull(implementation, "the given implementation identifier must not be null");

    if (implementation.isEmpty()) {
      throw new IllegalArgumentException("the given implementation identifier must not be empty");

    } else if (implementation.contains(IMPLEMENTATION_SEPARATOR)) {
      throw new IllegalArgumentException(
          "the given implementation identifier must not contain the separator character '"
              + IMPLEMENTATION_SEPARATOR
              + "'");
    }

    if (major < 0 || minor < 0 || micro < 0) {
      throw new IllegalArgumentException(
          "version contains negative numbers major: "
              + major
              + " minor: "
              + minor
              + " micro: "
              + micro);
    }

    this.implementation = implementation;

    this.major = major;
    this.minor = minor;
    this.micro = micro;
    this.qualifier = (qualifier == null) ? "" : qualifier;

    StringBuilder builder = new StringBuilder();
    builder.append(implementation);
    builder.append(IMPLEMENTATION_SEPARATOR);
    builder.append(major);
    builder.append(VERSION_SEPARATOR);
    builder.append(minor);
    builder.append(VERSION_SEPARATOR);
    builder.append(micro);

    if (!this.qualifier.isEmpty()) {
      builder.append(VERSION_SEPARATOR);
      builder.append(qualifier);
    }

    this.asString = builder.toString();
  }

  /**
   * Parses a version identifier from the specified string. Identifier that cannot be parsed will
   * return the {@link #INVALID} version instance.
   *
   * @param version string representation of the version identifier
   * @return a Version object representing the version identifier
   * @see #parseVersion(String, String)
   */
  public static Version parseVersion(String version) {
    Objects.requireNonNull(version, "Version must not be null");

    String trimmedVersion = version.trim();

    if (trimmedVersion.isEmpty()) return INVALID;

    String[] splitVersion = trimmedVersion.split(IMPLEMENTATION_SEPARATOR);

    if (splitVersion.length != 2) return INVALID;

    return parseVersion(splitVersion[0], splitVersion[1]);
  }

  /**
   * Parses a version number identifier from the specified version string in combination with the
   * given implementation identifier string. Identifier that cannot be parsed will return the {@link
   * #INVALID} version instance.
   *
   * @param implementation the Saros implementation identifier
   * @param versionNumbers string representation of the version number identifier
   * @return a Version object representing the version identifier
   */
  public static Version parseVersion(String implementation, String versionNumbers) {
    Objects.requireNonNull(implementation, "Version must not be null");
    Objects.requireNonNull(versionNumbers, "Version must not be null");

    String trimmedImplementation = implementation.trim();
    String trimmedVersion = versionNumbers.trim();

    if (trimmedImplementation.isEmpty()
        || trimmedImplementation.contains(IMPLEMENTATION_SEPARATOR)
        || trimmedVersion.isEmpty()) return INVALID;

    int major;
    int minor = 0;
    int micro = 0;
    String qualifier = "";

    StringTokenizer tokenizer = new StringTokenizer(trimmedVersion, VERSION_SEPARATOR, true);

    parse:
    try {
      major = Integer.parseInt(tokenizer.nextToken());

      if (!tokenizer.hasMoreTokens()) break parse;

      tokenizer.nextToken(); // delim
      minor = Integer.parseInt(tokenizer.nextToken());

      if (!tokenizer.hasMoreTokens()) break parse;

      tokenizer.nextToken(); // delim
      micro = Integer.parseInt(tokenizer.nextToken());

      if (!tokenizer.hasMoreTokens()) break parse;

      tokenizer.nextToken(); // delim
      qualifier = tokenizer.nextToken(""); // rest

    } catch (RuntimeException e) {
      return INVALID;
    }

    if (major < 0 || minor < 0 || micro < 0) {
      return INVALID;
    }

    return new Version(trimmedImplementation, major, minor, micro, qualifier);
  }

  /**
   * Returns the compatibility result from comparing this version to the given version.
   *
   * <p>Versions using different implementation identifiers are always seen as incompatible.
   *
   * <p>For determining version compatibility, only the major and minor version numbers are checked
   * if no qualifier is given for both versions. Differences in micro version number are always seen
   * as compatible in such cases.
   *
   * <p>If at least one of the compared versions contains a qualifier, the two versions have to
   * match completely (including the micro version number and the qualifier) to be seen as
   * compatible.
   *
   * @param other the version to compare against
   * @return the compatibility result from comparing this version to the given version
   */
  Compatibility determineCompatibilityWith(Version other) {
    if (!implementation.equals(other.implementation)) {
      return INCOMPATIBLE_IMPLEMENTATIONS;
    }

    if (!this.qualifier.isEmpty() || !other.qualifier.isEmpty()) {
      return this.hasSameVersionNumber(other) ? OK : QUALIFIER_MISMATCH;
    }

    int result;

    result = major - other.major;

    if (result != 0) {
      return valueOf(result);
    }

    result = minor - other.minor;

    return valueOf(result);
  }

  /**
   * Given a result from a numerical comparison of a version number will return the associated
   * Compatibility object.
   */
  private static Compatibility valueOf(int comparison) {
    switch (Integer.signum(comparison)) {
      case -1:
        return OLDER;
      case 0:
        return OK;
      case 1:
        return NEWER;
      default:
        return UNKNOWN;
    }
  }

  /**
   * Returns whether the version number is the same for this version and the given version. The
   * version number qualifier is included in this check. The implementation identifier is ignored
   * for this check.
   *
   * @param other the other version to compare with
   * @return whether the version number is the same for this version and the given version
   */
  private boolean hasSameVersionNumber(Version other) {
    return this.major == other.major
        && this.minor == other.minor
        && this.micro == other.micro
        && Objects.equals(this.qualifier, other.qualifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(implementation, major, minor, micro, qualifier);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Version other = (Version) obj;

    return Objects.equals(this.implementation, other.implementation) && hasSameVersionNumber(other);
  }

  @Override
  public String toString() {
    return asString;
  }
}
