package de.fu_berlin.inf.dpp.misc.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * Converts session- and IDE-dependent SPath to session- and IDE-independent XML representations,
 * and vice versa.
 *
 * <p><b>Example:</b> The XML representation of an {@link SPath} for a {@linkplain IProject project}
 * with id <code>"projA"</code> and a {@linkplain IPath relative path} <code>"src/Main.java"</code>:
 *
 * <pre>
 * &lt;SPath i="projA" p="src/Main.java" /&gt;
 * </pre>
 */
@Component
public class SPathConverter implements Converter, Startable {

  private static final Logger LOG = Logger.getLogger(SPathConverter.class);

  private static final String PATH = "p";
  private static final String PROJECT_ID = "i";

  private final ISarosSession session;
  private final IPathFactory pathFactory;

  private IReferencePointManager referencePointManager;

  public SPathConverter(ISarosSession session, IPathFactory pathFactory) {
    this.session = session;
    this.pathFactory = pathFactory;

    this.referencePointManager = session.getComponent(IReferencePointManager.class);

    if (this.referencePointManager == null)
      throw new IllegalStateException("ReferencePointManager is null. Session is not running");
  }

  @Override
  public void start() {
    ActivitiesExtension.PROVIDER.registerConverter(this);
  }

  @Override
  public void stop() {
    ActivitiesExtension.PROVIDER.unregisterConverter(this);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(SPath.class);
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

    SPath spath = (SPath) value;

    String i = session.getReferencePointID(spath.getReferencePoint());
    if (i == null) {
      LOG.error(
          "Could not retrieve project id for project '"
              + spath.getReferencePoint()
              + "'. Make sure you don't create activities for non-shared projects");
      return;
    }

    String p = URLCodec.encode(pathFactory.fromPath(spath.getProjectRelativePath()));

    writer.addAttribute(PROJECT_ID, i);
    writer.addAttribute(PATH, p);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    String i = reader.getAttribute(PROJECT_ID);
    String p = URLCodec.decode(reader.getAttribute(PATH));

    IProject project = referencePointManager.get(session.getReferencePoint(i));
    if (project == null) {
      LOG.error("Could not create SPath because there is no shared project for id '" + i + "'");
      return null;
    }

    IPath path = pathFactory.fromString(p);

    return new SPath(project, path);
  }
}
