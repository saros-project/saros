package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public class FileActivityTest extends AbstractResourceActivityTest {

    protected static final byte[] data = new byte[] { 'a', 'b', 'c' };

    @Test
    @Override
    public void testConversion() {
        // Mock remaining parameters
        List<Type> types = toListPlusNull(Type.values());
        List<SPath> oldPaths = toListPlusNull(SarosMocks.mockSPath());
        List<byte[]> datas = toListPlusNull(data);
        List<Purpose> purposes = toListPlusNull(Purpose.values());

        for (Type type : types) {
            for (SPath newPath : paths) {
                for (SPath oldPath : oldPaths) {
                    for (byte[] data : datas) {
                        for (Purpose purpose : purposes) {
                            FileActivity f;
                            try {
                                f = new FileActivity(source, type, newPath,
                                    oldPath, data, purpose);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }

                            testConversionAndBack(f);
                        }
                    }
                }
            }
        }
    }
}
