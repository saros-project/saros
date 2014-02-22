package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity.Type;

public class VCSActivityTest extends AbstractResourceActivityTest {

    @Test
    @Override
    public void testConversion() {
        for (Type type : toListPlusNull(Type.values())) {
            for (SPath path : paths) {
                for (String url : toListPlusNull("", "abc", "123")) {
                    for (String directory : toListPlusNull("", "abc", "123")) {
                        for (String param1 : toListPlusNull("", "abc", "123")) {
                            VCSActivity va;
                            try {
                                va = new VCSActivity(source, type, path, url,
                                    directory, param1);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }

                            testConversionAndBack(va);
                        }
                    }
                }
            }
        }
    }
}
