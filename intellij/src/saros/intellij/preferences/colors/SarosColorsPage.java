package saros.intellij.preferences.colors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.PlatformIcons;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.editor.colorstorage.ColorManager;
import saros.intellij.editor.colorstorage.ColorManager.IdentifiableColorKeys;
import saros.intellij.ui.Messages;

/** IntelliJ color scheme preferences for Saros specific colors. */
public class SarosColorsPage implements ColorSettingsPage {

  /** List of descriptors for colors that can be selected by users. */
  private static final List<AttributesDescriptor> COLOR_ATTRIBUTE_DESCRIPTORS;
  /** List of descriptors for colors not specific to Saros users. */
  private static final List<AttributesDescriptor> ADDITIONAL_ATTRIBUTE_DESCRIPTORS =
      ImmutableList.of(
          new AttributesDescriptor(
              Messages.ColorPreferences_default_user_text_selection_attribute_display_name,
              ColorManager.DEFAULT_COLOR_KEYS.getSelectionColorKey()),
          new AttributesDescriptor(
              Messages.ColorPreferences_default_user_text_contribution_attribute_display_name,
              ColorManager.DEFAULT_COLOR_KEYS.getContributionColorKey()));

  private static final Map<String, TextAttributesKey> HIGHLIGHT_MAP;

  static {
    final ImmutableList.Builder<AttributesDescriptor> builder = ImmutableList.builder();
    for (IdentifiableColorKeys colorKeys : ColorManager.COLOR_KEYS) {
      final int userDisplayID = colorKeys.getId() + 1;
      builder.add(
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_text_selection_attribute_display_name, userDisplayID),
              colorKeys.getSelectionColorKey()),
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_text_contribution_attribute_display_name,
                  userDisplayID),
              colorKeys.getContributionColorKey()));
    }
    COLOR_ATTRIBUTE_DESCRIPTORS = builder.build();
  }

  static {
    final ImmutableMap.Builder<String, TextAttributesKey> builder = ImmutableMap.builder();
    for (IdentifiableColorKeys identifiableColorKeys : ColorManager.COLOR_KEYS) {
      builder.put(
          "sel" + identifiableColorKeys.getId(), identifiableColorKeys.getSelectionColorKey());
      builder.put(
          "contrib" + identifiableColorKeys.getId(),
          identifiableColorKeys.getContributionColorKey());
    }
    builder.put("sel", ColorManager.DEFAULT_COLOR_KEYS.getSelectionColorKey());
    builder.put("contrib", ColorManager.DEFAULT_COLOR_KEYS.getContributionColorKey());
    HIGHLIGHT_MAP = builder.build();
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return PlatformIcons.CUSTOM_FILE_ICON;
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
    sb.append(MessageFormat.format(Messages.ColorPreferences_user_example_text, ""));
    for (IdentifiableColorKeys identifiableColorKeys : ColorManager.COLOR_KEYS) {
      sb.append(
          MessageFormat.format(
              Messages.ColorPreferences_user_example_text, identifiableColorKeys.getId()));
    }
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
