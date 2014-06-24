package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.session.User;

/**
 * An {@link IDEInteractionActivity} is used to represent awareness information
 * about opened dialogs or activated views.
 * <p>
 * {@link Element} is used to represent the IDE element with which the user
 * interacted, which can be a 'DIALOG' or a 'VIEW'. {@link Status} is used to
 * represent the kind of IDE activity. 'FOCUS' means, that a dialog was opened
 * or a view was activated and 'UNFOCUS' means, that a dialog was closed or a
 * view was deactivated.
 * */
@XStreamAlias("ideInteractionActivity")
public class IDEInteractionActivity extends AbstractActivity {

    /**
     * Represents the type of an IDE activity, namely whether the target, with
     * which the user interacted, was a dialog or a view.
     */
    public enum Element {
        DIALOG, VIEW
    }

    /**
     * Represents the state of an IDE activity, namely whether the dialog or
     * view was focused or unfocused.
     */
    public enum Status {
        FOCUS, UNFOCUS
    }

    @XStreamAsAttribute
    private final String title;

    @XStreamAsAttribute
    private final Element element;

    @XStreamAsAttribute
    private final Status status;

    public IDEInteractionActivity(User source, String title, Element element,
        Status status) {

        super(source);

        if (title == null)
            throw new IllegalArgumentException("Title must not be null");
        if (element == null)
            throw new IllegalArgumentException("Element must not be null");
        if (status == null)
            throw new IllegalArgumentException("Status must not be null");

        this.title = title;
        this.element = element;
        this.status = status;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (title != null) && (element != null)
            && (status != null);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public String getTitle() {
        return this.title;
    }

    public Element getElement() {
        return this.element;
    }

    public Status getStatus() {
        return this.status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(title);
        result = prime * result + ObjectUtils.hashCode(element);
        result = prime * result + ObjectUtils.hashCode(status);
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

        IDEInteractionActivity other = (IDEInteractionActivity) obj;

        if (!ObjectUtils.equals(this.title, other.title))
            return false;
        if (!ObjectUtils.equals(this.element, other.element))
            return false;
        if (!ObjectUtils.equals(this.status, other.status))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "IDEInteractionActivity(" + getSource() + " > " + element + " "
            + status + " " + title + ")";
    }
}