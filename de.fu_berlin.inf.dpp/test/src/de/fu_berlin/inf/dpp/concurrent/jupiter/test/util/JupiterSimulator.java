package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
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

        List<Request> inQueue = new LinkedList<Request>();

        Document document;

        public void generate(Operation o) {

            /* 1. execute locally */
            document.execOperation(o);

            Request request = algorithm.generateRequest(o, new JID("DUMMY"),
                new Path("DUMMY"));

            if (this == client) {
                server.inQueue.add(request);
            } else {
                client.inQueue.add(request);
            }
        }

        public void receive() throws TransformationException {
            Request request = inQueue.remove(0);
            Operation op = algorithm.receiveRequest(request);
            log.info("\n  " + "Transforming: " + request.getOperation() + " ("
                + request.getTimestamp() + ")\n" + "  into        : " + op);

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
