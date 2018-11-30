package de.fu_berlin.inf.dpp.stf.client;

import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.tester.SarosTester;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShutdownRemoteEclipse {

  private static final Logger LOGGER = Logger.getLogger(ShutdownRemoteEclipse.class.getName());

  public static void main(String... args) {

    List<AbstractTester> clients = new ArrayList<AbstractTester>();

    if (args.length == 0) {
      clients.addAll(Arrays.asList(SarosTester.values()));
    } else {
      for (String arg : args) {
        try {
          clients.add(SarosTester.valueOf(arg));
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, arg + " is not a tester", e);
        }
      }
    }

    for (AbstractTester tester : clients) {
      LOGGER.info("closing Eclipse instance of " + tester);
      try {
        tester.superBot().views().sarosView().disconnect();
        tester.remoteBot().closeAllEditors();
        tester.remoteBot().closeAllShells();
        tester.remoteBot().activateWorkbench();

        tester.remoteBot().menu("File").menu("Exit").click();
      } catch (Exception e) {
        if (!(e.getCause() instanceof SocketException))
          LOGGER.log(Level.SEVERE, "unable to shutdown eclipse instance of " + tester, e);
      }
    }
    LOGGER.info("DONE");
  }
}
