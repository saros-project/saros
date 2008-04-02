package de.fu_berlin.inf.dpp.test.jupiter;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;
import junit.framework.TestCase;

public class JupiterTestCase extends TestCase{
	static {
		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
	}
	
	protected SimulateNetzwork network;

	public void setUp() {
		network = new SimulateNetzwork();

	}
}
