package saros.concurrent.jupiter.test.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Assert;
import saros.activities.JupiterActivity;
import saros.activities.SPath;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.session.User;

public class JupiterSimulator {

  private static final Logger log = Logger.getLogger(JupiterSimulator.class.getName());

  public Peer client;

  public Peer server;

  public JupiterSimulator(String document) {

    IPath path = new PathFake("test");

    IProject project = createMock(IProject.class);
    IFile file = createNiceMock(IFile.class);

    expect(project.getFile(path)).andStubReturn(file);

    expect(file.getProject()).andStubReturn(project);
    expect(file.getProjectRelativePath()).andStubReturn(path);

    replay(project, file);

    client = new Peer(new Jupiter(true), document, file);
    server = new Peer(new Jupiter(false), document, file);
  }

  public class Peer {

    protected Algorithm algorithm;

    protected List<JupiterActivity> inQueue = new LinkedList<JupiterActivity>();

    protected Document document;

    private final IFile file;

    public Peer(Algorithm algorithm, String document, IFile file) {
      this.algorithm = algorithm;
      this.document = new Document(document, file);
      this.file = file;
    }

    public void generate(Operation operation) {

      /* 1. execute locally */
      document.execOperation(operation);

      User user = JupiterTestCase.createUser("DUMMY");

      JupiterActivity jupiterActivity =
          algorithm.generateJupiterActivity(operation, user, new SPath(file));

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
