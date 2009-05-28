package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.Jupiter;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterSimulator {

    private static final Logger log = Logger.getLogger(JupiterSimulator.class
        .getName());

    public Peer client;

    public Peer server;

    public JupiterSimulator(String document) {
        client = new Peer(new Jupiter(true), document);
        server = new Peer(new Jupiter(false), document);
    }

    public class Peer {

        public Peer(Algorithm algorithm, String document) {
            this.algorithm = algorithm;
            this.document = new Document(document);
        }

        Algorithm algorithm = new Jupiter(true);

        List<JupiterActivity> inQueue = new LinkedList<JupiterActivity>();

        Document document;

        public void generate(Operation o) {

            /* 1. execute locally */
            document.execOperation(o);

            JupiterActivity jupiterActivity = algorithm
                .generateJupiterActivity(o, new JID("DUMMY"), new Path("DUMMY"));

            if (this == client) {
                server.inQueue.add(jupiterActivity);
            } else {
                client.inQueue.add(jupiterActivity);
            }
        }

        public void receive() throws TransformationException {
            JupiterActivity jupiterActivity = inQueue.remove(0);
            Operation op = algorithm.receiveJupiterActivity(jupiterActivity);
            log.info("\n  " + "Transforming: " + jupiterActivity.getOperation()
                + " (" + jupiterActivity.getTimestamp() + ")\n"
                + "  into        : " + op);

            document.execOperation(op);
        }

        public String getDocument() {
            return this.document.toString();
        }
    }

    public void assertDocs(String string) {

        TestCase
            .assertEquals("Client mismatch: ", string, client.getDocument());
        TestCase
            .assertEquals("Client mismatch: ", string, client.getDocument());
        TestCase.assertEquals("Client Queue not empty:", 0, client.inQueue
            .size());
        TestCase.assertEquals("Server Queue not empty:", 0, server.inQueue
            .size());

    }

}
