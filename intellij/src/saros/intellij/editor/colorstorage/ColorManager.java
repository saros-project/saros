package saros.intellij.editor.colorstorage;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ color manager. This specifies the available colors that are used for Saros highlights
 * and that are available for configuration inside the Color Scheme preferences.
 */
public final class ColorManager {

  /**
   * List of keys for supported colors. A user can select one of these colors by referencing the
   * {@link IdentifiableColorKeys#getId()}.
   */
  public static final List<IdentifiableColorKeys> COLOR_KEYS;
  /** Color keys for the default colors. These are used if no */
  public static final ColorKeys DEFAULT_COLOR_KEYS = new DefaultColorKeys();
  /** Number of supported users. */
  private static final int USER_COUNT = 5;

  static {
    final ImmutableList.Builder<IdentifiableColorKeys> builder = ImmutableList.builder();
    for (int i = 0; i < USER_COUNT; i++) {
      builder.add(new IdentifiableColorKeys(i));
    }
    COLOR_KEYS = builder.build();
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
    return COLOR_KEYS
        .stream()
        .filter(x -> x.getId() == colorId)
        .findFirst()
        .map(ColorKeys.class::cast)
        .orElse(DEFAULT_COLOR_KEYS);
  }

  /**
   * Set of IntelliJ attribute keys that are used to define appearance of Saros users inside the
   * IDE.
   */
  public interface ColorKeys {

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

  /** A set of IntelliJ color keys that can be referenced using a color ID. */
  public static class IdentifiableColorKeys implements ColorKeys {
    /** ID of this color key set. Used for being referenced by a Saros user. */
    private final int id;

    @NotNull private final TextAttributesKey selectionColorKey;
    @NotNull private final TextAttributesKey contributionColorKey;

    private IdentifiableColorKeys(final int id) {
      final String selectionKeyName = "SAROS_TEXT_SELECTION_" + id;
      final String contributionKeyName = "SAROS_TEXT_CONTRIBUTION_" + id;
      this.selectionColorKey =
          TextAttributesKey.createTextAttributesKey(selectionKeyName, HighlighterColors.TEXT);
      this.contributionColorKey =
          TextAttributesKey.createTextAttributesKey(contributionKeyName, HighlighterColors.TEXT);
      this.id = id;
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
   * Holds IntelliJ color keys that are used if no matching {@link IdentifiableColorKeys} was found.
   * These color keys are used if user specific color keys have been requested, but no color keys
   * matching a given user ID were found.
   */
  private static final class DefaultColorKeys implements ColorKeys {

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
