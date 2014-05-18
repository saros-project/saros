package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Before;
import org.powermock.api.easymock.PowerMock;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public abstract class AbstractResourceActivityTest extends AbstractActivityTest {

    /**
     * Contains one mocked {@link SPath} and <code>null</code>
     */
    protected List<SPath> paths;

    @Override
    @Before
    public void setup() {
        setupDefaultMocks();

        SPath path = SarosMocks.prepareMockSPath();
        PowerMock.replay(path);

        paths = toListPlusNull(path);
    }

}
