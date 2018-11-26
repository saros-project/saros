package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import org.junit.Test;

public class ApplyRecordsTest {

  private final SXEDefaultRecordFactory recordFactory = new SXEDefaultRecordFactory();
  private final SXEController controller = new SXEController(recordFactory);
  private final DocumentRecord document = recordFactory.createDocument(controller);
  private final ElementRecord root = recordFactory.createRoot(document);

  {
    root.apply(document);
  }

  @Test
  public void testNewRecords() {

    ElementRecord r = recordFactory.createElementRecord(document, null, "rect");
    r.setParent(root);

    r.apply(document);

    assertTrue(document.contains(r));

    SetRecord rr = r.getRemoveRecord();

    rr.apply(document);

    assertFalse(r.isVisible());
  }

  @Test
  public void testConflictingSetRecords() {
    for (int i = 0; i < 3; i++) {
      testConflictingSetRecords(i);
    }
  }

  public void testConflictingSetRecords(int setRecordsBeforeConflict) {

    ElementRecord r = recordFactory.createElementRecord(document, null, "rect");

    r.setParent(root);

    r.apply(document);

    for (int i = 0; i < setRecordsBeforeConflict; i++) {

      SetRecord set0 = new SetRecord(r);
      set0.setPrimaryWeight((float) i);
      set0.apply(document);
    }

    float pwOrig = r.getPrimaryWeight();
    float pw1 = pwOrig + 1f;
    float pw2 = pwOrig + 2f;

    SetRecord set1 = new SetRecord(r, r.getVersion() + 1);
    set1.setPrimaryWeight(pw1);

    SetRecord set2 = new SetRecord(r, r.getVersion() + 1);
    set2.setPrimaryWeight(pw2);

    set1.apply(document);

    assertTrue(r.getPrimaryWeight() == pw1);

    set2.apply(document);

    assertTrue(r.getPrimaryWeight() == pwOrig);
  }
}
