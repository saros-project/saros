package de.fu_berlin.inf.dpp.stf.client.wrapper;

import de.fu_berlin.inf.dpp.stf.client.Tester;

public class Wrapper {
    protected Tester tester;

    // private RMIObjects rmiObjects;

    public Wrapper(Tester tester) {
        this.tester = tester;
        // rmiObjects = RMIObjects.getInstance(tester.host, tester.port);
    }

    // public RMIObjects remoteObjects() {
    // return rmiObjects;
    // }
}
