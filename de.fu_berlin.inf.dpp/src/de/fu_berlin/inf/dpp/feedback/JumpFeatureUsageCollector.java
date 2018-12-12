package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * This Collector collects data about the jump feature usage. It stores data about the total count
 * of jumps performed as well as the jumps to a user with {@link Permission#WRITE_ACCESS} or {@link
 * Permission#READONLY_ACCESS}.
 */
@Component(module = "feedback")
public class JumpFeatureUsageCollector extends AbstractStatisticCollector {

  /** Count of jumps to a user with {@link Permission#READONLY_ACCESS} position */
  private static final String KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS = "jumped.observer.count";

  /** Count of jumps to a user with {@link Permission#WRITE_ACCESS} position */
  private static final String KEY_JUMPED_TO_USER_WITH_WRITE_ACCESS = "jumped.driver.count";

  /** Total count of jumps */
  private static final String KEY_TOTAL_JUMP_COUNT = "jumped.count";

  private int jumpedToWriteAccessHolder = 0;
  private int jumpedToReadOnlyAccessHolder = 0;

  private final IEditorManager editorManager;

  private final ISharedEditorListener editorListener =
      new ISharedEditorListener() {

        @Override
        public void jumpedToUser(User target) {
          if (target.hasWriteAccess()) {
            jumpedToWriteAccessHolder++;
          } else {
            jumpedToReadOnlyAccessHolder++;
          }
        }
      };

  public JumpFeatureUsageCollector(
      StatisticManager statisticManager, ISarosSession session, IEditorManager editorManager) {
    super(statisticManager, session);
    this.editorManager = editorManager;
  }

  @Override
  protected void processGatheredData() {
    data.put(KEY_JUMPED_TO_USER_WITH_READONLY_ACCESS, jumpedToReadOnlyAccessHolder);
    data.put(KEY_JUMPED_TO_USER_WITH_WRITE_ACCESS, jumpedToWriteAccessHolder);
    data.put(KEY_TOTAL_JUMP_COUNT, jumpedToWriteAccessHolder + jumpedToReadOnlyAccessHolder);
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    editorManager.addSharedEditorListener(editorListener);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    editorManager.removeSharedEditorListener(editorListener);
  }
}
