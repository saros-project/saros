package saros.whiteboard.gef.editor;

import org.eclipse.gef.requests.CreationFactory;
import saros.whiteboard.gef.model.GEFRecordFactory;
import saros.whiteboard.sxe.records.NodeRecord;

/**
 * Adapts the SXE <code>GEFRecordFactory</code> for the GEF CreationFactory.
 *
 * <p>Important: the created new objects should be used for feedback only. Proper instantiating of
 * <code>NodeRecord</code>s to be inserted in the DOM tree is done by the <code>DocumentRecord
 * </code>
 *
 * @author jurke
 */
public class CombinedTargetRecordCreationFactory implements CreationFactory {

  private final String tag;
  private final GEFRecordFactory factory = new GEFRecordFactory();

  public CombinedTargetRecordCreationFactory(String tag) {
    this.tag = tag;
  }

  @Override
  // used to feedback the right shape, creation is done in the command
  public NodeRecord getNewObject() {
    return factory.createElementRecord(null, null, tag);
  }

  @Override
  public String getObjectType() {
    return tag;
  }
}
