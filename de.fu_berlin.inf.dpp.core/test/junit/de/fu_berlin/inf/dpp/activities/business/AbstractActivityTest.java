package de.fu_berlin.inf.dpp.activities.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public abstract class AbstractActivityTest {
    private static final String LOCAL_USER = "local@user";
    private static final String REMOTE_USER = "alice@jabber.org";

    /**
     * The standard source for all Activities
     */
    protected User source;
    /**
     * The standard receiver for all Activities
     */
    protected User target;
    /**
     * Contains {@link #target} and <code>null</code>
     */
    protected List<User> targets;

    /**
     * Used for conversion sanity check
     */
    private static boolean createdAtLeastOneActivity;

    /**
     * In case the concrete test class needs additional mocks, this method
     * should be overridden by calling <code>super.setup()<code>, too.
     */
    @Before
    public void setup() {
        setupDefaultMocks();
    }

    /**
     * Creates the mocks that every Activity test case may use. (Currently, the
     * default mocks are {@link #source} and {@link #target}, which are already
     * replayed by {@link SarosMocks#mockUser(JID)}).
     */
    protected void setupDefaultMocks() {
        JID sourceJid = new JID(LOCAL_USER);
        source = SarosMocks.mockUser(sourceJid);

        JID targetJid = new JID(REMOTE_USER);
        target = SarosMocks.mockUser(targetJid);

        targets = toListPlusNull(target);
    }

    /**
     * Checks whether the conversion from business object to serializable (and
     * back) works without alterations in the payload.<br>
     * <br>
     * Implementations are expected to create multiple variations of concrete
     * Activities which are given to {@link #testConversionAndBack(IActivity)}.
     * Therefore, it is not important whether a specific configurations fails
     * the ctor call (see "<code>continue</code>" in example below). This test
     * only ensures: When "it" (however weird the parameters might be) passes
     * the ctor, the conversion has to work in both ways.<br>
     * <br>
     * Example:
     * 
     * <pre>
     * public void testConversion() {
     *     for (int id = 0; id &lt; 10; i++) {
     *         MyActivity ma;
     *         try {
     *             ma = new MyActivity(source, target, id);
     *         } catch {IllegalArgumentException e} {
     *             // consumed, next configuration
     *             continue;
     *         }
     *         testConversionAndBack(ma);
     *     }
     * }
     * </pre>
     * 
     * The test will fail if <i>one</i> configuration fails the conversion test,
     * OR <i>not one</i> configuration ever passes.
     */
    public abstract void testConversion();

    /**
     * Performs the two conversions {@link IActivity} &rarr;
     * {@link IActivityDataObject} &rarr; {@link IActivity}. Compares the
     * outcome with the original and asserts equality.
     */
    protected void testConversionAndBack(IActivity orig) {
        IActivityDataObject ado = orig.getActivityDataObject();
        IActivity copy = ado.getActivity();

        assertEquals("conversion failed for: " + orig, orig, copy);

        createdAtLeastOneActivity = true;
    }

    /**
     * Initialize sanity check
     */
    @BeforeClass
    public static void setupClass() {
        createdAtLeastOneActivity = false;
    }

    /**
     * Sanity check for activitiy conversion test
     */
    @AfterClass
    public static void teardown() {
        assertTrue(
            "no activity was actually created and successfully converted",
            createdAtLeastOneActivity);
    }

    /**
     * Utility function for creating easy iterable lists from a wide range of
     * possible input values. May be used for testing many different ctor
     * configurations.
     * 
     * Example:
     * 
     * <pre>
     * // creates a list of all Enum values plus 'null'
     * List&lt;MyEnum&gt; values = toListPlusNull(MyEnum.values());
     * 
     * for (MyEnum val : values) {
     *     // do stuff with 'val'
     * }
     * </pre>
     * 
     * @param values
     * @return all original values and <code>null</code>
     */
    protected static <T> List<T> toListPlusNull(T... values) {
        ArrayList<T> newValues = new ArrayList<T>(Arrays.asList(values));
        newValues.add(null);
        return newValues;
    }
}