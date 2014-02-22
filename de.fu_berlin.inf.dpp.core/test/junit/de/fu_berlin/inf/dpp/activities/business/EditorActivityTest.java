package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;

public class EditorActivityTest extends AbstractResourceActivityTest {

    @Test
    @Override
    public void testConversion() {

        for (Type type : toListPlusNull(Type.values())) {
            for (SPath path : paths) {
                EditorActivity ea;
                try {
                    ea = new EditorActivity(source, type, path);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                testConversionAndBack(ea);
            }

        }
    }

}
