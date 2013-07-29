package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;
import de.fu_berlin.inf.dpp.util.FileUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class FileActivityTest extends AbstractResourceActivityTest {

    protected static final byte[] data = new byte[] { 'a', 'b', 'c' };
    protected long checksum = 12345L;

    @Test
    @Override
    public void testConversion() {
        // Mock remaining parameters
        List<Type> types = toListPlusNull(Type.values());
        List<SPath> oldPaths = toListPlusNull(SarosMocks.mockSPath());
        List<byte[]> datas = toListPlusNull(data);
        List<Purpose> purposes = toListPlusNull(Purpose.values());
        List<Long> checksums = toListPlusNull(new Long(1024L));

        for (Type type : types) {
            for (SPath newPath : paths) {
                for (SPath oldPath : oldPaths) {
                    for (byte[] data : datas) {
                        for (Purpose purpose : purposes) {
                            for (Long checksum : checksums) {
                                FileActivity f;
                                try {
                                    f = new FileActivity(source, type, newPath,
                                        oldPath, data, purpose, checksum);
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
}
