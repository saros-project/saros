package de.fu_berlin.inf.dpp.pico;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.util.pico.ChildContainer;
import de.fu_berlin.inf.dpp.util.pico.ChildContainerProvider;

/**
 * PicoContainer is used for resolving object dependencies. In this context, a
 * class X depends on Y if every X object requires exactly one Y object, and if
 * the life span of the X object fits into the life span of the Y object. I.e.
 * if an X object can't exist without its Y object.<br>
 * <br>
 * We use Dependency Injection instead of the Singleton pattern, see
 * http://misko.hevery.com/2008/08/21/where-have-all-the-singletons-gone/<br>
 * <br>
 * The EditorManager for example exists as long as the Saros plugin is running.
 * Since it has the same life cycle as Saros itself, it is managed by Saros'
 * container. Each instance of a SessionView uses the EditorManager.<br>
 * <br>
 * Scopes:<br>
 * Plugin<br>
 * Session (saros, chat, video, audio)<br>
 * View (roster, session, chat, video)<br>
 * Dialog (invitation process)<br>
 * [Shared project (some day)]<br>
 * <br>
 * Currently we only have one for Saros, and one per SessionView. We could use
 * one container for each scope, the session container would be a child of the
 * plugin container, and the outgoing invitation dialog a child of the session.
 */
public class UnderstandingPico {
    public static class A {
        @Inject
        private B b;
    }

    public static class B {
        //
    }

    public static class User {
        String name;
    }

    public interface ISession {
        public User getUser();
    }

    public static class Session implements ISession {
        @Inject
        private A a;
        @Inject
        private B b;
        @Inject
        private User user;

        public User getUser() {
            return user;
        }

    }

    @Test
    public void testInstantiation() {

        MutablePicoContainer container = new DefaultPicoContainer(
            new AnnotatedFieldInjection());
        container.addComponent(B.class);
        assertNotNull(container.getComponent(B.class));
    }

    @Test
    public void testInstantiationWithoutCaching() {
        // No caching means that getComponent will always instantiate a new
        // object.
        MutablePicoContainer container = new DefaultPicoContainer(
            new AnnotatedFieldInjection());
        container.addComponent(B.class);
        final B b = container.getComponent(B.class);
        assertTrue(b != container.getComponent(B.class));
    }

    @Test
    public void testAnnotatedFieldInjection() {

        MutablePicoContainer container = new DefaultPicoContainer(
            new AnnotatedFieldInjection());
        container.addComponent(A.class);
        container.addComponent(B.class);

        final A a = container.getComponent(A.class);
        assertNotNull(a);
        assertNotNull(a.b);
    }

    @Test
    public void testAnnotatedFieldInjectionWithoutCaching() {

        MutablePicoContainer container = new DefaultPicoContainer(
            new AnnotatedFieldInjection());
        container.addComponent(A.class);
        container.addComponent(B.class);

        final A a = container.getComponent(A.class);
        final A otherA = container.getComponent(A.class);
        assertNotNull(otherA);
        assertNotNull(otherA.b);
        assertTrue(a != otherA);
        assertTrue(a.b != otherA.b);
    }

    @Test
    public void testReinjection() {
        MutablePicoContainer container = new PicoBuilder(
            new AdaptingInjection()).withCaching().build();
        final Reinjector reinjector = new Reinjector(container);
        container.addComponent(B.class);

        A a = new A();

        assertNull(container.getComponent(A.class));
        assertNotNull(container.getComponent(B.class));

        container.addComponent(A.class, a);
        reinjector.reinject(A.class, new AnnotatedFieldInjection());
        container.removeComponent(A.class);

        assertNotNull(container.getComponent(B.class));
        assertNotNull(a.b);

        A otherA = new A();

        assertNull(container.getComponent(A.class));
        assertNotNull(container.getComponent(B.class));

        container.addComponent(A.class, otherA);
        reinjector.reinject(A.class, new AnnotatedFieldInjection());
        container.removeComponent(A.class);

        assertNotNull(otherA.b);
        assertTrue(a.b == otherA.b);
        assertNotNull(container.getComponent(B.class));
    }

    @Test
    public void testReinjectionWithChildContainer() {
        MutablePicoContainer container = new PicoBuilder(
            new AdaptingInjection()).withCaching().build();
        container.as(Characteristics.NO_CACHE).addAdapter(
            new ProviderAdapter(new ChildContainerProvider(container)));

        container.addComponent(B.class);

        {
            A a = new A();

            ChildContainer childContainer = container
                .getComponent(ChildContainer.class);
            assertTrue(container.getComponent(B.class) == childContainer
                .getComponent(B.class));

            assertNull(container.getComponent(A.class));
            assertNotNull(container.getComponent(B.class));

            // Put the A into the child container, and inject a B from the
            // parent
            // container into the A.
            childContainer.reinject(a);

            assertNotNull(container.getComponent(B.class));
            assertNotNull(a.b);
        }
        {
            A otherA = new A();

            ChildContainer childContainer = container
                .getComponent(ChildContainer.class);
            assertTrue(container.getComponent(B.class) == childContainer
                .getComponent(B.class));

            assertNotNull(container.getComponent(B.class));

            // Again, make sure we get the same B.
            childContainer.reinject(otherA);

            assertNull(container.getComponent(A.class));
            assertNotNull(otherA.b);
            assertTrue(container.getComponent(B.class) == otherA.b);
        }
    }

    @Test
    public void testScopedContainers() {
        MutablePicoContainer applicationContainer = new PicoBuilder(
            new AdaptingInjection()).withCaching().build();
        applicationContainer.addComponent(A.class);
        applicationContainer.addComponent(B.class);

        User frank = new User();
        frank.name = "Frank";

        MutablePicoContainer sessionContainer = new PicoBuilder(
            applicationContainer, new AdaptingInjection()).withCaching()
            .build();
        // configure session
        sessionContainer.addComponent(ISession.class, Session.class);
        sessionContainer.addComponent(frank);

        ISession isession = sessionContainer.getComponent(ISession.class);
        assertNotNull(isession);
        assertTrue(frank == isession.getUser());
        Session session = (Session) isession;
        assertNotNull(session.a);
        assertTrue(session.a == applicationContainer.getComponent(A.class));
        assertNotNull(session.b);
        assertTrue(session.b == applicationContainer.getComponent(B.class));
    }
}
