package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

/**
 * A {@link RefactoringActivity} is used to represent awareness information
 * about refactorings which are performed.
 * */
public class RefactoringActivity extends AbstractActivity {

    @XStreamAsAttribute
    private final String description;

    /**
     * Creates a new {@link RefactoringActivity} with the description of the
     * refactoring for the given user.
     * 
     * @param source
     *            The user who is the source (originator) of this activity
     * @param description
     *            The description of the refactoring
     */
    public RefactoringActivity(User source, String description) {
        super(source);

        if (description == null)
            throw new IllegalArgumentException("Description must not be null");

        this.description = description;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (description != null);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(description);
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

        RefactoringActivity other = (RefactoringActivity) obj;

        if (!ObjectUtils.equals(this.description, other.description))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "RefactoringActivity (description=" + description + ")";
    }
}