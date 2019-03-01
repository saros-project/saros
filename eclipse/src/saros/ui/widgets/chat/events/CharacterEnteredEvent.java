package saros.ui.widgets.chat.events;

import java.util.EventObject;

public class CharacterEnteredEvent extends EventObject {

  private static final long serialVersionUID = -1262502917485818862L;

  protected Character enteredCharacter;

  /**
   * Constructs a character entered event
   *
   * @param source The object on which the Event initially occurred.
   * @param character The entered character
   */
  public CharacterEnteredEvent(Object source, Character character) {
    super(source);

    this.enteredCharacter = character;
  }

  /**
   * Gets the entered character
   *
   * @return
   */
  public Character getEnteredCharacter() {
    return enteredCharacter;
  }
}
