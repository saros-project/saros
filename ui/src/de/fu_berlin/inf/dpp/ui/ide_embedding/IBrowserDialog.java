package de.fu_berlin.inf.dpp.ui.ide_embedding;

/** This interface abstracts GUI toolkit dialogs by providing just the needed functionality. */
public interface IBrowserDialog {

  /** Closes the dialog. */
  void close();

  /**
   * If the same dialog is openend again, the already existing one is activated and moved to the
   * foreground instead.
   */
  void reopen();
}
