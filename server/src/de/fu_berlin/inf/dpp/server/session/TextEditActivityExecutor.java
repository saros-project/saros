package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.server.editor.ServerEditorManager;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.picocontainer.Startable;

/** Executes TextEditActivities through the {@link ServerEditorManager}. */
public class TextEditActivityExecutor extends AbstractActivityConsumer implements Startable {

  private final ISarosSession session;
  private final ServerEditorManager editorManager;

  /**
   * Creates a TextEditActivityExecutor.
   *
   * @param session the current session
   * @param editorManager the editor manager
   */
  public TextEditActivityExecutor(ISarosSession session, ServerEditorManager editorManager) {

    this.session = session;
    this.editorManager = editorManager;
  }

  @Override
  public void start() {
    session.addActivityConsumer(this, Priority.ACTIVE);
  }

  @Override
  public void stop() {
    session.removeActivityConsumer(this);
  }

  @Override
  public void receive(TextEditActivity activity) {
    editorManager.applyTextEdit(activity);
  }
}
