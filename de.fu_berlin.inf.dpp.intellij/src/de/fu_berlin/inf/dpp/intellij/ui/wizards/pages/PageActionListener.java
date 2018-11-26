package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

/** Interface defines wizard page action listener structure */
public interface PageActionListener {
  /** User clicked on Back button */
  void back();

  /** User clicked on next or next button */
  void next();

  /** User clicked on back or cancel button */
  void cancel();
}
