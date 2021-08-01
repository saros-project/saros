package saros.intellij.editor.colorstorage;

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Intellij color manager. This specifies the available colors that are used for Saros highlights
 * and that are available for configuration inside the Color Scheme preferences.
 */
public final class ColorManager {

  /**
   * Key for the default selection color field. This value must match the option name for the
   * default selection color used in the color config files located in {@link colorSchemes}.
   */
  private static final String DEFAULT_SELECTION_KEY = "SAROS_DEFAULT_TEXT_SELECTION";
  /**
   * Key for the default contribution color field. This value must match the option name for the
   * default contribution color used in the color config files located in {@link colorSchemes}.
   */
  private static final String DEFAULT_CONTRIBUTION_KEY = "SAROS_DEFAULT_TEXT_CONTRIBUTION";
  /**
   * Prefix for the user selection color field. This value must match the prefix of the option names
   * for the user selection color used in the color config files located in {@link colorSchemes}.
   */
  private static final String SELECTION_KEY_PREFIX = "SAROS_TEXT_SELECTION_";
  /**
   * Prefix for the user contribution color field. This value must match the prefix of the option
   * names for the user contribution color used in the color config files located in {@link
   * colorSchemes}.
   */
  private static final String CONTRIBUTION_KEY_PREFIX = "SAROS_TEXT_CONTRIBUTION_";

  /** Number of supported users. */
  private static final int USER_COUNT = 5;

  /** Color keys for the default colors. Used if no color keys are available for a given id. */
  public static final ColorKeys DEFAULT_COLOR_KEYS = new DefaultColorKeys();
  /**
   * List of keys for supported colors. A user can select one of these colors by referencing the
   * {@link IdentifiableColorKeys#getId()}.
   */
  public static final List<IdentifiableColorKeys> COLOR_KEYS;

  /* Initialize COLOR_KEYS */
  static {
    List<IdentifiableColorKeys> colorKeys = new ArrayList<>();

    for (int i = 0; i < USER_COUNT; i++) {
      colorKeys.add(new IdentifiableColorKeys(i));
    }
    COLOR_KEYS = Collections.unmodifiableList(colorKeys);
  }

  private ColorManager() {}

  /**
   * Returns {@link ColorKeys} for the given {@code colorId}. If no keys were found for the given
   * {@code colorId}, this method returns the default {@link ColorKeys} instance.
   *
   * @param colorId ID of the color keys to retrieve.
   * @return The {@link ColorKeys} matching the given {@code colorId} or the default color keys.
   *     This method always returns a non-{@code null} value.
   */
  @NotNull
  public static ColorKeys getColorKeys(final int colorId) {
    return COLOR_KEYS.stream()
        .filter(x -> x.getId() == colorId)
        .findFirst()
        .map(ColorKeys.class::cast)
        .orElse(DEFAULT_COLOR_KEYS);
  }

  /**
   * Set of Intellij attribute keys that are used to define appearance of Saros users inside the
   * IDE.
   */
  public interface ColorKeys {

    /**
     * Returns the Intellij color key of the {@link TextAttributes} that should be used for
     * highlighting the selection of a user.
     *
     * @return attribute key used for text selection highlighting. Always non-null.
     */
    @NotNull
    TextAttributesKey getSelectionColorKey();

    /**
     * Returns the Intellij color key of the {@link TextAttributes} that should be used for
     * highlighting recent code contributions of a user.
     *
     * @return attribute key used for text contribution highlighting. Always non-null.
     */
    @NotNull
    TextAttributesKey getContributionColorKey();
  }

  /** A set of Intellij color keys that can be referenced using a color ID. */
  public static class IdentifiableColorKeys implements ColorKeys {
    /** ID of this color key set. Used for being referenced by a Saros user. */
    private final int id;

    @NotNull private final TextAttributesKey selectionColorKey;
    @NotNull private final TextAttributesKey contributionColorKey;

    private IdentifiableColorKeys(final int id) {
      this.id = id;

      final String selectionKeyName = SELECTION_KEY_PREFIX + id;
      final String contributionKeyName = CONTRIBUTION_KEY_PREFIX + id;

      this.selectionColorKey =
          TextAttributesKey.createTextAttributesKey(selectionKeyName, HighlighterColors.TEXT);
      this.contributionColorKey =
          TextAttributesKey.createTextAttributesKey(contributionKeyName, HighlighterColors.TEXT);
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
     * Returns the ID of this color.
     *
     * @return ID of this color.
     */
    public int getId() {
      return id;
    }
  }

  /**
   * Holds Intellij color keys that are used if no matching {@link IdentifiableColorKeys} was found.
   * These color keys are used if user specific color keys have been requested, but no color keys
   * matching a given user ID were found.
   */
  private static final class DefaultColorKeys implements ColorKeys {

    private static final TextAttributesKey DEFAULT_SELECTION_COLOR_KEY =
        TextAttributesKey.createTextAttributesKey(DEFAULT_SELECTION_KEY, HighlighterColors.TEXT);
    private static final TextAttributesKey DEFAULT_CONTRIBUTION_COLOR_KEY =
        TextAttributesKey.createTextAttributesKey(DEFAULT_CONTRIBUTION_KEY, HighlighterColors.TEXT);

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
