package saros.intellij.preferences.colors;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.editor.colorstorage.ColorManager;
import saros.intellij.editor.colorstorage.ColorManager.IdentifiableColorKeys;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;

/** IntelliJ color scheme preferences for Saros specific colors. */
public class SarosColorsPage implements ColorSettingsPage {

  /** List of descriptors for colors that can be selected by users. */
  private static final List<AttributesDescriptor> COLOR_ATTRIBUTE_DESCRIPTORS;
  /** List of descriptors for colors not specific to Saros users. */
  private static final List<AttributesDescriptor> ADDITIONAL_ATTRIBUTE_DESCRIPTORS;
  /** Maps specific tags to the possible text attribute keys. Used for the demo text. */
  private static final Map<String, TextAttributesKey> HIGHLIGHT_MAP;

  /* Initialize COLOR_ATTRIBUTE_DESCRIPTORS */
  static {
    List<AttributesDescriptor> attributesDescriptors = new ArrayList<>();

    for (IdentifiableColorKeys colorKeys : ColorManager.COLOR_KEYS) {
      final int userDisplayID = colorKeys.getId() + 1;

      attributesDescriptors.add(
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_text_selection_attribute_display_name, userDisplayID),
              colorKeys.getSelectionColorKey()));

      attributesDescriptors.add(
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_text_contribution_attribute_display_name,
                  userDisplayID),
              colorKeys.getContributionColorKey()));
    }

    COLOR_ATTRIBUTE_DESCRIPTORS = Collections.unmodifiableList(attributesDescriptors);
  }

  /* Initialize ADDITIONAL_ATTRIBUTE_DESCRIPTORS */
  static {
    List<AttributesDescriptor> attributesDescriptors = new ArrayList<>();

    attributesDescriptors.add(
        new AttributesDescriptor(
            Messages.ColorPreferences_default_user_text_selection_attribute_display_name,
            ColorManager.DEFAULT_COLOR_KEYS.getSelectionColorKey()));

    attributesDescriptors.add(
        new AttributesDescriptor(
            Messages.ColorPreferences_default_user_text_contribution_attribute_display_name,
            ColorManager.DEFAULT_COLOR_KEYS.getContributionColorKey()));

    ADDITIONAL_ATTRIBUTE_DESCRIPTORS = Collections.unmodifiableList(attributesDescriptors);
  }

  /* Initialize HIGHLIGHT_MAP */
  static {
    Map<String, TextAttributesKey> textAttributesKeyMap = new HashMap<>();

    textAttributesKeyMap.put("sel", ColorManager.DEFAULT_COLOR_KEYS.getSelectionColorKey());
    textAttributesKeyMap.put("contrib", ColorManager.DEFAULT_COLOR_KEYS.getContributionColorKey());

    for (IdentifiableColorKeys identifiableColorKeys : ColorManager.COLOR_KEYS) {
      textAttributesKeyMap.put(
          "sel" + identifiableColorKeys.getId(), identifiableColorKeys.getSelectionColorKey());

      textAttributesKeyMap.put(
          "contrib" + identifiableColorKeys.getId(),
          identifiableColorKeys.getContributionColorKey());
    }

    HIGHLIGHT_MAP = Collections.unmodifiableMap(textAttributesKeyMap);
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return IconManager.SAROS_ICON;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    // We're returning blind text, so no syntax highlighting is required.
    return new PlainSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    final StringBuilder sb = new StringBuilder();

    sb.append(createDemoTextForColorKey(null));

    sb.append(System.lineSeparator()).append(System.lineSeparator());

    for (IdentifiableColorKeys identifiableColorKeys : ColorManager.COLOR_KEYS) {

      sb.append(createDemoTextForColorKey(identifiableColorKeys));

      sb.append(System.lineSeparator()).append(System.lineSeparator());
    }

    sb.setLength(sb.length() - 2);

    return sb.toString();
  }

  /**
   * Creates the demo text for the user represented by the given identifiable color keys.
   *
   * @param identifiableColorKeys the identifiable color keys or <code>null</code> for the default
   *     user
   * @return the demo text for the given identifiable color keys
   */
  @NotNull
  private String createDemoTextForColorKey(@Nullable IdentifiableColorKeys identifiableColorKeys) {
    StringBuilder sb = new StringBuilder();

    String userID;
    String usedDescription;

    if (identifiableColorKeys == null) {
      userID = "";
      usedDescription = Messages.ColorPreferences_default_user_description;
    } else {
      userID = Integer.toString(identifiableColorKeys.getId());

      int shownID = (identifiableColorKeys.getId() + 1);
      usedDescription = MessageFormat.format(Messages.ColorPreferences_user_description, shownID);
    }

    sb.append(
        MessageFormat.format(
            Messages.ColorPreferences_user_example_text_contribution, userID, usedDescription));

    sb.append(System.lineSeparator());

    sb.append(
        MessageFormat.format(
            Messages.ColorPreferences_user_example_text_selection, userID, usedDescription));

    return sb.toString();
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return HIGHLIGHT_MAP;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return Stream.of(COLOR_ATTRIBUTE_DESCRIPTORS, ADDITIONAL_ATTRIBUTE_DESCRIPTORS)
        .flatMap(Collection::stream)
        .toArray(AttributesDescriptor[]::new);
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return Messages.ColorPreferences_display_name;
  }
}
