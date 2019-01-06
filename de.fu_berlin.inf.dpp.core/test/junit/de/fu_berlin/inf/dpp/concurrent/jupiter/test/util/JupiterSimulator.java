package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.session.User;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.apache.log4j.Logger;

public class JupiterSimulator {

  private static final Logger log = Logger.getLogger(JupiterSimulator.class.getName());

  public Peer client;

  public Peer server;

  public JupiterSimulator(String document) {

    IReferencePoint referencePoint = createMock(IReferencePoint.class);
    replay(referencePoint);

    IProject project = createMock(IProject.class);
    expect(project.getReferencePoint()).andStubReturn(referencePoint);
    replay(project);
    IPath path = new PathFake("test");

    client = new Peer(new Jupiter(true), document, project, path);
    server = new Peer(new Jupiter(false), document, project, path);
  }

  public class Peer {

    protected Algorithm algorithm;

    protected List<JupiterActivity> inQueue = new LinkedList<JupiterActivity>();

    protected Document document;

    protected IPath path;

    protected IProject project;

    public Peer(Algorithm algorithm, String document, IProject project, IPath path) {
      this.algorithm = algorithm;
      this.document = new Document(document, project, path);
      this.project = project;
      this.path = path;
    }

    public void generate(Operation operation) {

      /* 1. execute locally */
      document.execOperation(operation);

      User user = JupiterTestCase.createUser("DUMMY");

      JupiterActivity jupiterActivity =
          algorithm.generateJupiterActivity(operation, user, new SPath(project.getReferencePoint(), path));

      if (this == client) {
        server.inQueue.add(jupiterActivity);
      } else {
        client.inQueue.add(jupiterActivity);
      }
    }

    public void receive() throws TransformationException {
      JupiterActivity jupiterActivity = inQueue.remove(0);
      Operation op = algorithm.receiveJupiterActivity(jupiterActivity);
      log.info(
          "\n  "
              + "Transforming: "
              + jupiterActivity.getOperation()
              + " ("
              + jupiterActivity.getTimestamp()
              + ")\n"
              + "  into        : "
              + op);

      document.execOperation(op);
    }

    public String getDocument() {
      return this.document.toString();
    }
  }

  public void assertDocs(String string) {

    Assert.assertEquals("Client mismatch: ", string, client.getDocument());
    Assert.assertEquals("Client mismatch: ", string, client.getDocument());
    Assert.assertEquals("Client Queue not empty:", 0, client.inQueue.size());
    Assert.assertEquals("Server Queue not empty:", 0, server.inQueue.size());
  }
}
