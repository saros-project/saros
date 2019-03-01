/** */
package saros.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import saros.activities.IActivity;
import saros.activities.QueueItem;
import saros.session.User;

public class TransformationResult {

  protected User localUser;

  public TransformationResult(User localUser) {
    this.localUser = localUser;
  }

  public void add(QueueItem queueItem) {
    ArrayList<User> toSendViaNetwork = new ArrayList<User>();
    for (User user : queueItem.recipients) {
      if (user.isLocal()) {
        executeLocally.add(queueItem.activity);
      } else {
        toSendViaNetwork.add(user);
      }
    }

    if (toSendViaNetwork.isEmpty()) return;

    this.sendToPeers.add(new QueueItem(toSendViaNetwork, queueItem.activity));
  }

  public List<IActivity> executeLocally = new ArrayList<IActivity>();

  public List<QueueItem> sendToPeers = new ArrayList<QueueItem>();

  public void addAll(TransformationResult other) {
    if (!ObjectUtils.equals(other.localUser, this.localUser)) {
      throw new IllegalArgumentException(
          "can only merge two transformation result objects if they are managing the same local user");
    }
    this.executeLocally.addAll(other.executeLocally);
    this.sendToPeers.addAll(other.sendToPeers);
  }

  public void addAll(List<QueueItem> items) {
    for (QueueItem item : items) {
      add(item);
    }
  }

  public List<IActivity> getLocalActivities() {
    return executeLocally;
  }

  public List<QueueItem> getSendToPeers() {
    return sendToPeers;
  }
}
