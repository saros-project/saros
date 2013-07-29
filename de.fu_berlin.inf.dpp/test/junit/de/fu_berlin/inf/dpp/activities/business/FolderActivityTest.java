package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;

public class FolderActivityTest extends AbstractResourceActivityTest {

    @Override
    @Test
    public void testConversion() {
        List<Type> types = toListPlusNull(Type.values());

        for (Type type : types) {
            for (SPath path : paths) {
                FolderActivity fa;
                try {
                    fa = new FolderActivity(source, type, path);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                testConversionAndBack(fa);
            }
        }
    }

}
