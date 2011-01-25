/**
 * 
 */
package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.project.internal.SarosSession.QueueItem;

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

        if (toSendViaNetwork.isEmpty())
            return;

        this.sendToPeers
            .add(new QueueItem(toSendViaNetwork, queueItem.activity));
    }

    public List<IActivity> executeLocally = new ArrayList<IActivity>();

    public List<QueueItem> sendToPeers = new ArrayList<QueueItem>();

    public void addAll(TransformationResult other) {
        if (!ObjectUtils.equals(other.localUser, this.localUser)) {
            throw new IllegalArgumentException(
                "Can only merge two Transformation Result objects if they are managing the same local buddy");
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