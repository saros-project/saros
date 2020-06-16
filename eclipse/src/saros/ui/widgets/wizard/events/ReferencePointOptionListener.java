package saros.ui.widgets.wizard.events;

import saros.ui.widgets.wizard.ReferencePointOptionComposite;

/** Listener for changes to a {@link ReferencePointOptionComposite}. */
public interface ReferencePointOptionListener {
  /**
   * This method is called when a value in a field of the currently chosen option of how to
   * represent the reference point in the local workspace changed.
   *
   * <p>The new state can be requested using {@link ReferencePointOptionComposite#getResult()}.
   *
   * @param composite the changed composite
   */
  void valueChanged(ReferencePointOptionComposite composite);

  /**
   * This method is called when the selected option of how to represent the reference point in the
   * local workspace changed.
   *
   * <p>The new state can be requested using {@link ReferencePointOptionComposite#getResult()}.
   *
   * @param composite the changed composite
   */
  void selectedOptionChanged(ReferencePointOptionComposite composite);
}
