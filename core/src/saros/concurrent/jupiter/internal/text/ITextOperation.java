package saros.concurrent.jupiter.internal.text;

import saros.concurrent.jupiter.Operation;

/**
 * An ITextOperation is an operation which describes a text change, either insert or delete, so
 * there is a position and a text
 */
public interface ITextOperation extends Operation {

  /** @return the position of the text to be deleted / inserted */
  public int getPosition();

  /** @return the length of the text to be deleted / inserted */
  public int getTextLength();

  /** @return the text to be deleted / inserted */
  public String getText();
}
