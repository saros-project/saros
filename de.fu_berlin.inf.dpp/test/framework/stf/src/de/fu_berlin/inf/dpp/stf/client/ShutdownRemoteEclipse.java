package de.fu_berlin.inf.dpp.stf.client;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.tester.SarosTester;

public class ShutdownRemoteEclipse {

    private static Logger log = Logger.getLogger(ShutdownRemoteEclipse.class);

    public static void main(String... args) {

        BasicConfigurator.configure();
        List<AbstractTester> clients = new ArrayList<AbstractTester>();

        if (args.length == 0) {
            clients.addAll(Arrays.asList(SarosTester.values()));
        } else {
            for (String arg : args) {
                try {
                    clients.add(SarosTester.valueOf(arg));
                } catch (Exception e) {
                    log.warn(arg + " is not a tester", e);
                }
            }
        }

        for (AbstractTester tester : clients) {
            log.info("closing Eclipse instance of " + tester);
            try {
                tester.superBot().views().sarosView().disconnect();
                tester.remoteBot().closeAllEditors();
                tester.remoteBot().closeAllShells();
                tester.remoteBot().activateWorkbench();

                tester.remoteBot().menu("File").menu("Exit").click();
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketException))
                    log.error("unable to shutdown eclipse instance of "
                        + tester, e);
            }
        }
        log.info("DONE");
    }
}
