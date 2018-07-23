package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import java.util.List;

public interface IActivityQueuer {

  /**
   * Processes the incoming {@linkplain IActivity activities} and decides which {@linkplain
   * IResourceActivity resource related activities} should be queued.
   *
   * @param activities
   * @return dequeued {@linkplain IResourceActivity IResourceActivities} and not queued {@linkplain
   *     IActivity IActivities} from input {@code activities} list
   */
  public List<IActivity> process(final List<IActivity> activities);
}
