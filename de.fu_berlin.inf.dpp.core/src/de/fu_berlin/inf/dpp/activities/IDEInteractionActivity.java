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
 * interacted, which can be a 'DIALOG' or a 'VIEW'.
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

    @XStreamAsAttribute
    private final String title;

    @XStreamAsAttribute
    private final Element element;

    /**
     * Creates a new {@link IDEInteractionActivity} with the title of the ide
     * element with which was interacted by the given user and the kind of
     * element, which can be a dialog or a view ({@link Element}).
     * 
     * @param title
     *            The title of the ide element with which was interacted
     * @param element
     *            The kind of element, which can be a dialog or a view (
     *            {@link Element})
     * */
    public IDEInteractionActivity(User source, String title, Element element) {

        super(source);

        if (title == null)
            throw new IllegalArgumentException("Title must not be null");
        if (element == null)
            throw new IllegalArgumentException("Element must not be null");

        this.title = title;
        this.element = element;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (title != null) && (element != null);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(title);
        result = prime * result + ObjectUtils.hashCode(element);
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

        return true;
    }

    @Override
    public String toString() {
        return "IDEInteractionActivity(" + getSource() + " > " + element + " "
            + title + ")";
    }
}