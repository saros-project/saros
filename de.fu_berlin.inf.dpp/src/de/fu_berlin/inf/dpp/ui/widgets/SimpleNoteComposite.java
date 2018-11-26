package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/** An easy to use {@link NoteComposite} that can only contain text. */
public class SimpleNoteComposite extends NoteComposite {

  protected Label contentLabel;

  public SimpleNoteComposite(Composite parent, int style, Image image, String text) {
    this(parent, style);
    setImage(image);
    setText(text);
  }

  public SimpleNoteComposite(Composite parent, int style, int systemImage, String text) {
    this(parent, style);
    setImage(systemImage);
    setText(text);
  }

  public SimpleNoteComposite(Composite parent, int style, Image image) {
    this(parent, style);
    setImage(image);
  }

  public SimpleNoteComposite(Composite parent, int style, int systemImage) {
    this(parent, style);
    setImage(systemImage);
  }

  public SimpleNoteComposite(Composite parent, int style, String text) {
    this(parent, style);
    setText(text);
  }

  public SimpleNoteComposite(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  public Layout getContentLayout() {
    return new FillLayout();
  }

  @Override
  public void createContent(Composite parent) {
    contentLabel = new Label(parent, SWT.WRAP);
  }

  /**
   * Sets the content's image
   *
   * @param systemImage SWT constant that declares a system image (e.g. {@link
   *     SWT#ICON_INFORMATION})
   */
  @Override
  public void setImage(int systemImage) {
    this.illustratedComposite.setImage(systemImage);
  }

  /** Sets the content's image */
  @Override
  public void setImage(Image image) {
    this.illustratedComposite.setImage(image);
  }

  /**
   * Sets the content's text
   *
   * @param text
   */
  public void setText(String text) {
    this.contentLabel.setText(text);
    this.layout();
  }

  @Override
  public void setForeground(Color color) {
    super.setForeground(color);
    this.contentLabel.setForeground(color);
  }
}
