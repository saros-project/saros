package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * FIXME The way this XML is created is ugly and fragile
 */
public class RequestPacketExtension implements PacketExtension {

    public static PacketFilter getFilter() {
        return new PacketExtensionFilter(ELEMENT, NAMESPACE);
    }

    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String ELEMENT = "request";

    public static final String SESSION_ID = "sessionID";

    public static final String PATH = "path";

    public static final String JID = "jid";

    public static final String SIDE_ID = "sideID";

    public static final String VECTOR_TIME = "vectortime";

    public static final String INSERT_OP = "insert";

    public static final String DELETE_OP = "delete";

    public static final String NO_OP = "no_op";

    public static final String SPLIT_OP = "split";

    public static final String TIMESTAMP_OP = "time";

    public static final String LOCAL_TIME = "localtime";

    public static final String REMOTE_TIME = "remotetime";

    public static final String POSITION = "position";

    public static final String ORIGIN = "origin";

    public static final String TEXT = "text";

    public static final String LENGTH = "length";

    private Request request;

    private String sessionID;

    public RequestPacketExtension(String sessionID, Request request) {
        this.sessionID = sessionID;
        this.request = request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return this.request;
    }

    public String getElementName() {
        return RequestPacketExtension.ELEMENT;
    }

    public String getNamespace() {
        return RequestPacketExtension.NAMESPACE;
    }

    public String toXML() {
        if (this.request == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(getElementName());
        sb.append(" xmlns=\"").append(getNamespace() + "\"");
        sb.append(">");

        sb.append(sessionIdToXML());
        sb.append(pathToXML());
        sb.append(jidToXML());
        sb.append(sideIDToXML());
        sb.append(vectorTimeToXML());
        operationToXML(sb, request.getOperation());

        sb.append("</").append(getElementName()).append(">");

        return sb.toString();
    }

    private String sessionIdToXML() {
        return "<" + RequestPacketExtension.SESSION_ID + ">" + sessionID + "</"
            + RequestPacketExtension.SESSION_ID + ">";
    }

    private String pathToXML() {
        return "<" + RequestPacketExtension.PATH + ">"
            + Util.escapeCDATA(this.request.getEditorPath().toPortableString())
            + "</" + RequestPacketExtension.PATH + ">";
    }

    private String jidToXML() {
        return "<" + RequestPacketExtension.JID + ">"
            + Util.escapeCDATA(this.request.getJID().toString()) + "</"
            + RequestPacketExtension.JID + ">";
    }

    private String sideIDToXML() {
        return "<" + RequestPacketExtension.SIDE_ID + ">"
            + this.request.getSiteId() + "</" + RequestPacketExtension.SIDE_ID
            + ">";
    }

    private String vectorTimeToXML() {
        int[] components = this.request.getTimestamp().getComponents();
        return "<" + RequestPacketExtension.VECTOR_TIME + " local=\""
            + components[0] + "\" remote=\"" + components[1] + "\"" + "/>";
    }

    private void operationToXML(StringBuilder sb, Operation op) {
        if (op instanceof InsertOperation) {
            insertOp(sb, (InsertOperation) op);
        }
        if (op instanceof DeleteOperation) {
            deleteOp(sb, (DeleteOperation) op);
        }
        if (op instanceof NoOperation) {
            noOp(sb, (NoOperation) op);
        }
        if (op instanceof TimestampOperation) {
            timestampOp(sb, (TimestampOperation) op);
        }
        if (op instanceof SplitOperation) {
            splitOp(sb, (SplitOperation) op);
        }
    }

    private void timestampOp(StringBuilder sb, TimestampOperation op) {
        sb.append("<" + RequestPacketExtension.TIMESTAMP_OP + "/>");
    }

    private void noOp(StringBuilder sb, NoOperation op) {
        sb.append("<" + RequestPacketExtension.NO_OP + "/>");
    }

    private void splitOp(StringBuilder sb, SplitOperation split) {
        sb.append("<" + RequestPacketExtension.SPLIT_OP + ">");
        operationToXML(sb, split.getFirst());
        operationToXML(sb, split.getSecond());
        sb.append("</" + RequestPacketExtension.SPLIT_OP + ">");
    }

    private void insertOp(StringBuilder sb, InsertOperation ins) {

        sb.append("<" + RequestPacketExtension.INSERT_OP + " "
            + RequestPacketExtension.POSITION + "=\"" + ins.getPosition()
            + "\"" + " " + RequestPacketExtension.ORIGIN + "=\""
            + ins.getOrigin() + "\"" + ">");
        sb.append(Util.escapeCDATA(ins.getText()));
        sb.append("</" + RequestPacketExtension.INSERT_OP + ">");
    }

    private void deleteOp(StringBuilder sb, DeleteOperation del) {

        sb.append("<" + RequestPacketExtension.DELETE_OP + " "
            + RequestPacketExtension.POSITION + "=\"" + del.getPosition()
            + "\"" + ">");
        sb.append(Util.escapeCDATA(del.getText()));
        sb.append("</" + RequestPacketExtension.DELETE_OP + ">");
    }

    public String getSessionID() {
        return sessionID;
    }

}
