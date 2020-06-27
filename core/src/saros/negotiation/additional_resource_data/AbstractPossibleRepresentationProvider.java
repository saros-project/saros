package saros.negotiation.additional_resource_data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;
import saros.negotiation.AdditionalResourceDataFactory;
import saros.negotiation.AdditionalResourceDataFactory.AdditionalResourceDataProvider;
import saros.negotiation.ResourceNegotiationData;
import saros.util.EscapeUtil;

/**
 * Abstract class providing the logic to add the possible representation of the reference point to
 * the additional resource data.
 *
 * <p>The key {@link #POSSIBLE_REPRESENTATIONS_KEY} is used to store the value in the additional
 * resource data. The data can be accessed using {@link
 * #getPossibleRepresentations(ResourceNegotiationData)}.
 *
 * <p>The resolution of the possible representations is provided by the implementing class through
 * {@link #computePossibleRepresentations(IReferencePoint)}.
 */
public abstract class AbstractPossibleRepresentationProvider
    implements AdditionalResourceDataProvider {

  private static final Logger log = Logger.getLogger(AbstractPossibleRepresentationProvider.class);

  private static final String POSSIBLE_REPRESENTATIONS_KEY = "POSSIBLE_REPRESENTATIONS";

  private static final String ESCAPE_CHARACTER = "\\";

  private static final String DELIMITER_OUTER = "|";
  private static final String DELIMITER_INNER = ":";

  private static final EscapeUtil ESCAPE_OUTER = new EscapeUtil(DELIMITER_OUTER, ESCAPE_CHARACTER);
  private static final EscapeUtil ESCAPE_INNER = new EscapeUtil(DELIMITER_INNER, ESCAPE_CHARACTER);

  public AbstractPossibleRepresentationProvider(
      AdditionalResourceDataFactory additionalResourceDataFactory) {

    additionalResourceDataFactory.registerAdditionalResourceDataProvider(this);
  }

  /**
   * Returns a list of possible local representations of the reference point resource.
   *
   * <p>The representations are defined through a pair containing the name of a relevant reference
   * object and a path of the reference point resource relative to the reference object.
   *
   * <p>The paths contained in the mapping must always use Unix separators.
   *
   * @param referencePoint the reference point
   * @return a list of possible local representations of the reference point resource
   */
  protected abstract List<Pair<String, String>> computePossibleRepresentations(
      IReferencePoint referencePoint);

  @Override
  public Map<String, String> getMapping(IReferencePoint referencePoint) {
    List<Pair<String, String>> possibleRepresentations =
        computePossibleRepresentations(referencePoint);

    String flattenedPossibleRepresentations =
        possibleRepresentations
            .stream()
            .map(
                pair ->
                    ESCAPE_INNER.escape(pair.getLeft())
                        + DELIMITER_INNER
                        + ESCAPE_INNER.escape(pair.getRight()))
            .map(ESCAPE_OUTER::escape)
            .collect(Collectors.joining(DELIMITER_OUTER));

    return Collections.singletonMap(POSSIBLE_REPRESENTATIONS_KEY, flattenedPossibleRepresentations);
  }

  /**
   * Returns the possible representations contained in the given resource negotiation data.
   *
   * <p>The paths contained in the mapping always use Unix separators.
   *
   * @param resourceNegotiationData the resource negotiation data
   * @return the possible representations contained in the given resource negotiation data or an
   *     empty list of no such representations are specified by the negotiation data
   */
  public static List<Pair<String, String>> getPossibleRepresentations(
      ResourceNegotiationData resourceNegotiationData) {
    String flattenedPossibleRepresentation =
        resourceNegotiationData.getAdditionalResourceData().get(POSSIBLE_REPRESENTATIONS_KEY);

    if (flattenedPossibleRepresentation == null) {
      return Collections.emptyList();
    }

    return split(flattenedPossibleRepresentation);
  }

  /**
   * Splits and unescapes given flattened possible representation string. Returns the possible
   * representations contained in the given string.
   *
   * @param flattenedPossibleRepresentations the flattened possible representation string contained
   *     in the resource negotiation data
   * @return the possible representations contained in the given string
   */
  private static List<Pair<String, String>> split(String flattenedPossibleRepresentations) {
    return ESCAPE_OUTER
        .splitAsStream(flattenedPossibleRepresentations)
        .map(ESCAPE_OUTER::unescape)
        .map(AbstractPossibleRepresentationProvider::splitInner)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Splits and unescapes the joined possible representation value pair using {@link #ESCAPE_INNER}.
   *
   * @param joinedPossibleRepresentation the joined possible representation value pair to split
   * @return the value pair represented by the given string or <code>null</code> if the split
   *     operation failed
   */
  private static ImmutablePair<String, String> splitInner(String joinedPossibleRepresentation) {
    String[] values = ESCAPE_INNER.split(joinedPossibleRepresentation);

    if (values.length != 1 && values.length != 2) {
      log.error(
          "Failed to split received list of possible representation options: "
              + "Wrong number of split results - joined string: "
              + joinedPossibleRepresentation
              + ", split result: "
              + Arrays.toString(values));

      return null;
    }

    String name = values[0];

    String path;
    if (values.length == 2) {
      path = values[1];
    } else {
      path = "";
    }

    return new ImmutablePair<>(ESCAPE_INNER.unescape(name), ESCAPE_INNER.unescape(path));
  }
}
