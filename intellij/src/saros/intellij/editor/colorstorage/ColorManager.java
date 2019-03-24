package saros.intellij.editor.colorstorage;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.annotations.AnnotationManager.AnnotationType;

/**
 * IntelliJ color manager. This specifies the available colors that are used for Saros
 * highlights and that are available for configuration inside the Color Scheme preferences.
 */
public final class ColorManager {

  /** List of keys for supported user colors. */
  public static final List<SarosUserColorKeys> USER_COLOR_KEYS;
  /** Color keys for the default (unknown) user. */
  public static final UserColorKeys DEFAULT_USER_COLOR_KEYS = new DefaultUserColorKeys();
  /** Number of supported users. */
  private static final int USER_COUNT = 5;

  static {
    final ImmutableList.Builder<SarosUserColorKeys> builder = ImmutableList.builder();
    for (int i = 0; i < USER_COUNT; i++) {
      builder.add(new SarosUserColorKeys(i));
    }
    USER_COLOR_KEYS = builder.build();
  }

  private ColorManager() {}

  /**
   * Returns the color for the given userID. Returns the DEFAULT_COLOR when there is no color for
   * the userID.
   *
   * @return the color for the given userID.
   */
  public static TextAttributes getHighlightColor(
      @NotNull final EditorColorsScheme colorsScheme,
      final int userID,
      @NotNull final AnnotationType annotationType) {

    // Find the matching color keys based on the user ID, or fall back to the default user.
    final UserColorKeys colorKeys =
        USER_COLOR_KEYS
            .stream()
            .filter(x -> x.getUserID() == userID)
            .findFirst()
            .map(UserColorKeys.class::cast)
            .orElse(DEFAULT_USER_COLOR_KEYS);

    // Extract the relevant color key, based on the annotation type.
    final TextAttributesKey highlightColorKey;
    switch (annotationType) {
      case SELECTION_ANNOTATION:
        highlightColorKey = colorKeys.getSelectionColorKey();
        break;
      case CONTRIBUTION_ANNOTATION:
        highlightColorKey = colorKeys.getContributionColorKey();
        break;
      default:
        throw new IllegalStateException("Unknown AnnotationType: " + annotationType);
    }

    return colorsScheme.getAttributes(highlightColorKey);
  }

  /** Holds IntelliJ color keys specific to a user. */
  public interface UserColorKeys {

    /**
     * Returns the IntelliJ color key of the {@link TextAttributes} that should be used for
     * highlighting the selection of a user.
     *
     * @return attribute key used for text selection highlighting. Always non-null.
     */
    @NotNull
    TextAttributesKey getSelectionColorKey();

    /**
     * Returns the IntelliJ color key of the {@link TextAttributes} that should be used for
     * highlighting recent code contributions of a user.
     *
     * @return attribute key used for text contribution highlighting. Always non-null.
     */
    @NotNull
    TextAttributesKey getContributionColorKey();
  }

  /** Holds IntelliJ color keys for a specific Saros user, based on the user's ID. */
  public static class SarosUserColorKeys implements UserColorKeys {

    private final TextAttributesKey selectionColorKey;
    private final TextAttributesKey contributionColorKey;
    private int userID;

    SarosUserColorKeys(final int userID) {
      final String selectionKeyName = "SAROS_USER_" + userID + "_TEXT_SELECTION";
      final String contributionKeyName = "SAROS_USER_" + userID + "_TEXT_CONTRIBUTION";
      this.selectionColorKey =
          TextAttributesKey.createTextAttributesKey(selectionKeyName, HighlighterColors.TEXT);
      this.contributionColorKey =
          TextAttributesKey.createTextAttributesKey(contributionKeyName, HighlighterColors.TEXT);
      this.userID = userID;
    }

    @NotNull
    @Override
    public TextAttributesKey getSelectionColorKey() {
      return selectionColorKey;
    }

    @NotNull
    @Override
    public TextAttributesKey getContributionColorKey() {
      return contributionColorKey;
    }

    /**
     * Returns the ID of the user to whom these color keys belong.
     *
     * @return ID of a user.
     */
    public int getUserID() {
      return userID;
    }
  }

  /**
   * Holds IntelliJ color keys for the default user. These color keys are used if user specific
   * color keys have been requested, but no color keys matching a given user ID were found.
   */
  public static final class DefaultUserColorKeys implements UserColorKeys {

    private static final TextAttributesKey DEFAULT_SELECTION_COLOR_KEY =
        TextAttributesKey.createTextAttributesKey(
            "SAROS_DEFAULT_TEXT_SELECTION", HighlighterColors.TEXT);
    private static final TextAttributesKey DEFAULT_CONTRIBUTION_COLOR_KEY =
        TextAttributesKey.createTextAttributesKey(
            "SAROS_DEFAULT_TEXT_CONTRIBUTION", HighlighterColors.TEXT);

    @NotNull
    @Override
    public TextAttributesKey getSelectionColorKey() {
      return DEFAULT_SELECTION_COLOR_KEY;
    }

    @NotNull
    @Override
    public TextAttributesKey getContributionColorKey() {
      return DEFAULT_CONTRIBUTION_COLOR_KEY;
    }
  }
}
