package de.fu_berlin.inf.dpp.awareness.actions;

import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class represents different action types the user executed, like running
 * a test, executing a refactoring, creating a file or interacting with an ide
 * element (dialog or view).
 * 
 * It offers the type of actions ({@link ActionType}) and getters to get special
 * information about the different action types.
 * 
 * NOTE: Depending on the {@link ActionType}, some variables are null, because
 * they are not used. For example, if the type is <code>ADD_CREATEDFILE</code>,
 * the following getters will return null:
 * <ul>
 * <li><code>getRefactoringDescription()</code></li>
 * <li><code>getIdeTitle()</code></li>
 * <li><code>getIdeElementType()</code></li>
 * <li><code>getTestRunName()</code></li>
 * <li><code>getTestRunState()</code></li>
 * </ul>
 * */
public class ActionTypeDataHolder {

    private final ActionType type;
    private final User user;

    // Information about refactorings
    private final String refactoringDescription;

    // Information about ide interaction
    private final String ideTitle;
    private final Element ideElementType;

    // Information about created files
    private final String createdFileName;

    // Information about test runs
    private final String testRunName;
    private final State testRunState;

    /**
     * Creates a new {@link ActionTypeDataHolder} which represents a file
     * creation action or a refactoring action. Depending on the
     * {@link ActionType}, this holder represents a file creation or a
     * refactoring.
     * 
     * @param user
     *            The user which executed this action
     * @param type
     *            The type of action, which can be a file creation or a
     *            refactoring (see {@link ActionType})
     * @param fileNameOrDescription
     *            If the <code>type</code> ({@link ActionType}) is
     *            <code>ActionType.ADD_CREATEDFILE</code>, this string
     *            represents the name of the created file. Otherwise, if the
     *            <code>type</code> is <code>ActionType.ADD_REFACTORING</code>,
     *            this string represents the description of the executed
     *            refactoring.
     */
    public ActionTypeDataHolder(User user, ActionType type,
        String fileNameOrDescription) {

        if (user == null)
            throw new IllegalArgumentException("User must not be null");
        if (type == null)
            throw new IllegalArgumentException("Type must not be null");

        this.user = user;
        this.type = type;
        this.ideTitle = null;
        this.ideElementType = null;
        this.testRunName = null;
        this.testRunState = null;

        switch (type) {
        case ADD_CREATEDFILE:
            this.createdFileName = fileNameOrDescription;
            this.refactoringDescription = null;
            break;
        case ADD_REFACTORING:
            this.createdFileName = null;
            this.refactoringDescription = fileNameOrDescription;
            break;
        default:
            this.createdFileName = null;
            this.refactoringDescription = null;
            break;
        }
    }

    /**
     * Creates a new {@link ActionTypeDataHolder} which represents an
     * interaction with ide elements, like dialogs or views.
     * 
     * @param user
     *            The user which executed this action
     * @param type
     *            The type of action, which can be an interaction with ide
     *            elements, like dialogs or views (see {@link ActionType})
     * @param title
     *            The title of the ide element with which the user interacted
     * @param element
     *            The kind of the ide element with which the user interacted,
     *            which can be a dialog or a view (see {@link Element})
     */
    public ActionTypeDataHolder(User user, ActionType type, String title,
        Element element) {
        this.user = user;
        this.type = type;
        this.ideTitle = title;
        this.ideElementType = element;
        this.createdFileName = null;
        this.refactoringDescription = null;
        this.testRunName = null;
        this.testRunState = null;
    }

    /**
     * Creates a new {@link ActionTypeDataHolder} which represents a test run.
     * 
     * @param user
     *            The user which executed this action
     * @param type
     *            The type of action, which can be an interaction with ide
     *            elements, like dialogs or views (see {@link ActionType})
     * @param name
     *            The name of the currently running or finished test run
     * @param state
     *            The state of the currently running or finished test run (see
     *            {@link State})
     */
    public ActionTypeDataHolder(User user, ActionType type, String name,
        State state) {
        this.user = user;
        this.type = type;
        this.testRunName = name;
        this.testRunState = state;
        this.ideTitle = null;
        this.ideElementType = null;
        this.createdFileName = null;
        this.refactoringDescription = null;
    }

    /**
     * Returns the type of the action for which this object stands for.
     * 
     * @return The type of the {@link ActionTypeDataHolder}
     * @see ActionType
     * */
    public ActionType getType() {
        return type;
    }

    /**
     * Returns user who is the source (originator) of the action for which this
     * {@link ActionTypeDataHolder} stands for.
     * 
     * @return The user who is the source (originator) of this activity
     * */
    public User getUser() {
        return user;

    }

    /**
     * Returns the description of a refactoring or <code>null</code>, if the
     * {@link ActionType} of this object is not <code>ADD_REFACTORING</code>.
     * 
     * @return The description of a refactoring or <code>null</code>, if the
     *         {@link ActionType} of this object is not
     *         <code>ADD_REFACTORING</code>.
     * */
    public String getRefactoringDescription() {
        return refactoringDescription;
    }

    /**
     * Returns the title of an opened dialog or activated view or
     * <code>null</code>, if the {@link ActionType} of this object is not
     * <code>ADD_IDEELEMENT</code>.
     * 
     * @return The title of an opened dialog or activated view or
     *         <code>null</code>, if the {@link ActionType} of this object is
     *         not <code>ADD_IDEELEMENT</code>.
     * */
    public String getIdeTitle() {
        return ideTitle;
    }

    /**
     * Returns the type of the opened or activated IDE element, which can be a
     * dialog or a view (see {@link ActionType}), or <code>null</code>, if the
     * {@link ActionType} of this object is not <code>ADD_IDEELEMENT</code>.
     * 
     * @return The type of the opened IDE element, which can be a dialog or a
     *         view (see {@link ActionType}), or <code>null</code>, if the
     *         {@link ActionType} of this object is not
     *         <code>ADD_IDEELEMENT</code>.
     * */
    public Element getIdeElementType() {
        return ideElementType;
    }

    /**
     * Returns the name of the created file or <code>null</code>, if the
     * {@link ActionType} of this object is not <code>ADD_CREATEDFILE</code>.
     * 
     * @return The name of the created file or <code>null</code>, if the
     *         {@link ActionType} of this object is not
     *         <code>ADD_CREATEDFILE</code>.
     * */
    public String getCreatedFileName() {
        return createdFileName;
    }

    /**
     * Returns the name of the test run or <code>null</code>, if the
     * {@link ActionType} of this object is not <code>ADD_TESTRUN</code>.
     * 
     * @return The name of the test run or <code>null</code>, if the
     *         {@link ActionType} of this object is not <code>ADD_TESTRUN</code>
     *         .
     * */
    public String getTestRunName() {
        return testRunName;
    }

    /**
     * Returns the state of the test run (see {@link State}), or
     * <code>null</code>, if the {@link ActionType} of this object is not
     * <code>ADD_TESTRUN</code>.
     * 
     * @return The state of the test run (see {@link State}), or
     *         <code>null</code>, if the {@link ActionType} of this object is
     *         not <code>ADD_TESTRUN</code>.
     * */
    public State getTestRunState() {
        return testRunState;
    }
}