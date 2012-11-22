package de.fu_berlin.inf.dpp.pico;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.AnnotatedFieldInjection;

/**
 * This class shows you the different injection methods and what we would like
 * to see in our code base and what <b>NOT</b>.
 */
public class UnderstandingPicoInjection {

    public static interface INetwork {
        void sendMessage(String message);
    }

    public static interface IDatabase {
        String performQuery(String query);
    }

    public static class RealNetworkImpl implements INetwork {

        @Override
        public void sendMessage(String message) {
            if ((System.currentTimeMillis() & 1L) == 0)
                throw new RuntimeException("not connected");

            System.out.println("send message: " + message);
        }
    }

    public static class RealDatabaseImpl implements IDatabase {

        @Override
        public String performQuery(String message) {
            if ((System.currentTimeMillis() & 1L) == 0)
                throw new RuntimeException("database error");

            System.out.println("successfully performed query: " + message);
            return "ok";
        }
    }

    public static class FakeNetworkImpl implements INetwork {

        @Override
        public void sendMessage(String message) {
            System.out.println("send message: " + message);
        }
    }

    public static class FakeDatabaseImpl implements IDatabase {

        @Override
        public String performQuery(String message) {
            System.out.println("successfully performed query: " + message);
            return "ok";
        }
    }

    /* Now lets see the usage of different injections methods */

    private static interface Businesslogic {
        void performLogic();
    }

    public static class OtherDeveloperWouldHateYouForThat implements
        Businesslogic {
        private RealNetworkImpl network;
        private RealDatabaseImpl database;

        public OtherDeveloperWouldHateYouForThat(RealNetworkImpl network,
            RealDatabaseImpl database) {
            this.network = network;
            this.database = database;
        }

        @Override
        public void performLogic() {
            network
                .sendMessage(database.performQuery("SELECT * FROM ANYTHING"));
        }
    }

    public static class OtherDeveloperWouldHateYouForThatEvenMore implements
        Businesslogic {
        @Inject
        private RealNetworkImpl network;

        @Inject
        private RealDatabaseImpl database;

        @Override
        public void performLogic() {
            network
                .sendMessage(database.performQuery("SELECT * FROM ANYTHING"));
        }
    }

    @Test
    public void testOtherDeveloperWouldHateYouForThat() {
        Businesslogic logic = new OtherDeveloperWouldHateYouForThat(
            new RealNetworkImpl(), new RealDatabaseImpl());

        logic.performLogic();
    }

    @Test
    public void testOtherDeveloperWouldHateYouForThatEvenMoreNoContainer() {
        Businesslogic logic = new OtherDeveloperWouldHateYouForThatEvenMore();

        /*
         * this will throw a NPE and will be a nightmare if you do integration
         * tests with multiple components
         */
        logic.performLogic();
    }

    @Test
    public void testOtherDeveloperWouldHateYouForThatEvenMoreMustCreateContainer() {

        MutablePicoContainer container = new DefaultPicoContainer(
            new AnnotatedFieldInjection());

        container.addComponent(RealNetworkImpl.class);
        container.addComponent(RealDatabaseImpl.class);

        container.addComponent(OtherDeveloperWouldHateYouForThatEvenMore.class);

        Businesslogic logic = container.getComponent(Businesslogic.class);

        logic.performLogic();
    }

    /* so how to do it right ? Simple, just use CTOR injection */

    public static class TDDProvedClass implements Businesslogic {
        private INetwork network;
        private IDatabase database;

        public TDDProvedClass(INetwork network, IDatabase database) {
            this.network = network;
            this.database = database;
        }

        @Override
        public void performLogic() {
            network
                .sendMessage(database.performQuery("SELECT * FROM ANYTHING"));
        }
    }

    @Test
    public void testComponentInIsolation() {
        Businesslogic logic = new TDDProvedClass(new FakeNetworkImpl(),
            new FakeDatabaseImpl());

        logic.performLogic();
    }

    /* you can even use the class in integration tests easily */

    @Test
    public void testComponentIntegration() {
        MutablePicoContainer container = new PicoBuilder(
            new AdaptingInjection()).withCaching().build();

        container.addComponent(FakeNetworkImpl.class);
        container.addComponent(FakeDatabaseImpl.class);

        container.addComponent(TDDProvedClass.class);

        /* ... add more components that are needed for the integration test */

        Businesslogic logic = container.getComponent(Businesslogic.class);

        logic.performLogic();
    }
}
