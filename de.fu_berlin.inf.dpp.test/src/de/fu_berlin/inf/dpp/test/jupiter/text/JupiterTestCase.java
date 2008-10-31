package de.fu_berlin.inf.dpp.test.jupiter.text;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;

public class JupiterTestCase extends TestCase {
    static {
	PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    }

    public JupiterTestCase() {

    }

    public JupiterTestCase(String method) {
	super(method);
    }

    protected SimulateNetzwork network;

    @Override
    public void setUp() {
	network = new SimulateNetzwork();

    }
}
