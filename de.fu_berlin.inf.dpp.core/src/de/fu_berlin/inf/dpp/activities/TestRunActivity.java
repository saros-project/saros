package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

/**
 * A {@link TestRunActivity} is used to represent awareness information about
 * started or finished test runs.
 * <p>
 * {@link State} is used to represent the state of the currently running test
 * run and can have following values:
 * <ul>
 * <li><b>UNDEFINED</b>: The test run has started, thus the state is not defined
 * yet.</li>
 * <li><b>OK</b>: The test run has finished and was successful.</li>
 * <li><b>FAILURE</b>: The test run has finished and was not successful.</li>
 * <li><b>ERROR</b>: The test run has finished and has an error, thus there is
 * no result.</li>
 * </ul>
 * */
public class TestRunActivity extends AbstractActivity {

    /**
     * Represents the state of a test run, which was launched by the user.
     */
    public enum State {
        UNDEFINED, OK, ERROR, FAILURE
    }

    @XStreamAsAttribute
    private final String name;

    @XStreamAsAttribute
    private final State state;

    /**
     * Creates a new {@link TestRunActivity} with the name of the test run and
     * the state of it for the given user.
     * 
     * @param source
     *            The user who is the source (originator) of this activity
     * @param name
     *            The name of the test class
     * @param state
     *            The state of the currently running test (see {@link State})
     */
    public TestRunActivity(User source, String name, State state) {
        super(source);

        if (name == null)
            throw new IllegalArgumentException("Name must not be null");
        if (state == null)
            throw new IllegalArgumentException("State must not be null");

        this.name = name;
        this.state = state;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (name != null) && (state != null);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(name);
        result = prime * result + ObjectUtils.hashCode(state);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;

        TestRunActivity other = (TestRunActivity) obj;

        if (!ObjectUtils.equals(this.name, other.name))
            return false;
        if (!ObjectUtils.equals(this.state, other.state))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "TestRunActivity (name=" + name + ", state=" + state + ")";
    }
}
