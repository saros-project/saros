package de.fu_berlin.inf.dpp.stf.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.tester.SarosTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class ShutdownRemoteEclipse {

    private static Logger log = Logger.getLogger(ShutdownRemoteEclipse.class);

    public static void main(String... args) {

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
            try {
                tester.superBot().views().sarosView().disconnect();
                Util.deleteAllProjects(tester);
                tester.remoteBot().menu("File").menu("Exit").click();
            } catch (Exception e) {
                log.error("unable to shut down eclipse instance of " + tester,
                    e);
            }
        }
    }
}
