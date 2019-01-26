package de.fu_berlin.inf.dpp.git;

import static org.easymock.EasyMock.isA;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

public class GitManagerTest {

  private ISarosSession session;
  private IActivityConsumer consumer;
  private User user;

  @Before
  public void createSessionsMocks() {
    session = EasyMock.createMock(ISarosSession.class);
    session.addActivityProducer(isA(GitManager.class));
    session.removeActivityProducer(isA(GitManager.class));

    session.addActivityConsumer(
        isA(IActivityConsumer.class), isA(IActivityConsumer.Priority.class));
    EasyMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                consumer = (IActivityConsumer) EasyMock.getCurrentArguments()[0];
                return null;
              }
            });
    session.removeActivityConsumer(isA(IActivityConsumer.class));

    user = new User(new JID("alice"), true, true, 1, -1);

    user.setInSession(true);

    session.getLocalUser();
    EasyMock.expectLastCall().andStubReturn(user);

    session.getUser(user.getJID());
    EasyMock.expectLastCall().andStubReturn(user);

    EasyMock.replay(session);
  }

  @Test
  public void testCreation() {
    GitManager gitManager = new GitManager(session);
    gitManager.start();
    gitManager.stop();
    EasyMock.verify(session);
  }
}
