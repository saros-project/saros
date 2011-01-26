package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;

public class TestApplyRecords {

	@Test
	public void testNewRecords() {
		SXEDefaultRecordFactory recordFactory = new SXEDefaultRecordFactory();
		SXEController controller = new SXEController(recordFactory);

		DocumentRecord document = recordFactory.createDocument(controller);
		ElementRecord root = recordFactory.createRoot(document);

		root.apply(document);

		ElementRecord r = recordFactory.createElementRecord(document, null,
				"rect");
		r.setParent(root);

		r.apply(document);

		assertTrue(document.contains(r));

		// RemoveRecord rr = new RemoveRecord(r);

		// rr.apply(document);

		assertFalse(document.contains(r));
		// assertTrue(document.isRemoved(r.getRid()));

	}

}
