package org.eclipse.swtbot.swt.finder.matchers;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import org.eclipse.swt.widgets.Widget;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Matches widgets if the getText() method of the widget matches the specified regex after all
 * mnemonics are striped out.
 */
public final class WithMnemonicRegex<T extends Widget> extends AbstractMatcher<T> {

  /** The regular expression string. */
  private Pattern pattern;

  /**
   * Constructs the regular expression matcher with the given regular expression string.
   *
   * @param regex the regex to match on the {@link org.eclipse.swt.widgets.Widget}
   */
  private WithMnemonicRegex(String regex) {
    pattern = Pattern.compile(regex);
  }

  private String getText(Object obj)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    return WithText.getText(obj)
        .replaceAll("&", "")
        .split("\t")[0]; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  protected boolean doMatch(Object obj) {
    try {
      return pattern.matcher(getText(obj)).matches();
    } catch (Exception e) {
      // do nothing
    }
    return false;
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("with regex '")
        .appendValue(pattern)
        .appendText("'"); // $NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Matches a widget that has the specified regex after all mnemonics are striped out.
   *
   * @param regex the label.
   * @return a matcher.
   */
  @Factory
  public static <T extends Widget> Matcher<T> withMnemonicRegex(String regex) {
    return new WithMnemonicRegex<T>(regex);
  }
}
