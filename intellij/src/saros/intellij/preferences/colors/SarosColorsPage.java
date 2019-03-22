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
import saros.intellij.editor.colorstorage.ColorManager.SarosUserColorKeys;
import saros.intellij.ui.Messages;

/** IntelliJ color scheme preferences for Saros specific colors. */
public class SarosColorsPage implements ColorSettingsPage {

  /** List of descriptors for colors specific for each user. */
  private static final List<AttributesDescriptor> USER_ATTRIBUTE_DESCRIPTORS;

  static {
    final ImmutableList.Builder<AttributesDescriptor> builder = ImmutableList.builder();
    for (SarosUserColorKeys colorKeys : ColorManager.USER_COLOR_KEYS) {
      final int userDisplayID = colorKeys.getUserID() + 1;
      builder.add(
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_user_text_selection_attribute_display_name,
                  userDisplayID),
              colorKeys.getSelectionColorKey()),
          new AttributesDescriptor(
              MessageFormat.format(
                  Messages.ColorPreferences_user_text_contribution_attribute_display_name,
                  userDisplayID),
              colorKeys.getContributionColorKey()));
    }
    USER_ATTRIBUTE_DESCRIPTORS = builder.build();
  }

  /** List of descriptors for colors not specific to Saros users. */
  private static final List<AttributesDescriptor> ADDITIONAL_ATTRIBUTE_DESCRIPTORS =
      ImmutableList.of(
          new AttributesDescriptor(
              Messages.ColorPreferences_default_user_text_selection_attribute_display_name,
              ColorManager.DEFAULT_USER_COLOR_KEYS.getSelectionColorKey()),
          new AttributesDescriptor(
              Messages.ColorPreferences_default_user_text_contribution_attribute_display_name,
              ColorManager.DEFAULT_USER_COLOR_KEYS.getContributionColorKey()));

  private static final Map<String, TextAttributesKey> HIGHLIGHT_MAP;

  static {
    final ImmutableMap.Builder<String, TextAttributesKey> builder = ImmutableMap.builder();
    for (SarosUserColorKeys userColorKeys : ColorManager.USER_COLOR_KEYS) {
      builder.put("sel" + userColorKeys.getUserID(), userColorKeys.getSelectionColorKey());
      builder.put("contrib" + userColorKeys.getUserID(), userColorKeys.getContributionColorKey());
    }
    builder.put("sel", ColorManager.DEFAULT_USER_COLOR_KEYS.getSelectionColorKey());
    builder.put("contrib", ColorManager.DEFAULT_USER_COLOR_KEYS.getContributionColorKey());
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
    for (SarosUserColorKeys userColorKeys : ColorManager.USER_COLOR_KEYS) {
      sb.append(
          MessageFormat.format(
              Messages.ColorPreferences_user_example_text, userColorKeys.getUserID()));
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
    return Stream.of(USER_ATTRIBUTE_DESCRIPTORS, ADDITIONAL_ATTRIBUTE_DESCRIPTORS)
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
