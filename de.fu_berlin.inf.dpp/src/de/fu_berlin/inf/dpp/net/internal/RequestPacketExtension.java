package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;
import java.util.Vector;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;

public class RequestPacketExtension implements PacketExtension {
    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String ELEMENT = "request";

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

    public RequestPacketExtension(Request request) {
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

	StringBuffer buf = new StringBuffer();
	buf.append("<").append(getElementName());
	buf.append(" xmlns=\"").append(getNamespace() + "\"");

	// buf.append(" xmlns=\"").append(getNamespace()).append("\">");

	buf.append(">");

	buf.append(pathToXML());
	buf.append(jidToXML());
	buf.append(sideIDToXML());
	buf.append(vectorTimeToXML());
	buf.append(operationToXML());

	// buf.append(requestToXML());

	buf.append("</").append(getElementName()).append(">");
	return buf.toString();
	// return "<request></request>";
    }

    private String pathToXML() {
	return "<" + RequestPacketExtension.PATH + ">"
		+ this.request.getEditorPath() + "</"
		+ RequestPacketExtension.PATH + ">";
    }

    private String jidToXML() {
	return "<" + RequestPacketExtension.JID + ">" + this.request.getJID()
		+ "</" + RequestPacketExtension.JID + ">";
    }

    private String sideIDToXML() {
	return "<" + RequestPacketExtension.SIDE_ID + ">"
		+ this.request.getSiteId() + "</"
		+ RequestPacketExtension.SIDE_ID + ">";
    }

    private String vectorTimeToXML() {
	String xml = "";
	Timestamp timestamp = this.request.getTimestamp();
	xml += "<" + RequestPacketExtension.VECTOR_TIME + " local=\""
		+ timestamp.getComponents()[0] + "\" remote=\""
		+ timestamp.getComponents()[1] + "\"" + "/>";
	return xml;
    }

    private String operationToXML() {
	Operation op = this.request.getOperation();
	String xml = "";
	if (op instanceof InsertOperation) {
	    xml += insertOp(op);
	}
	if (op instanceof DeleteOperation) {
	    xml += deleteOp(op);
	}
	if (op instanceof NoOperation) {
	    // NoOperation no = (NoOperation) op;
	    xml += "<" + RequestPacketExtension.NO_OP + "/>";
	}
	if (op instanceof TimestampOperation) {
	    xml += "<" + RequestPacketExtension.TIMESTAMP_OP + "/>";
	}
	if (op instanceof SplitOperation) {
	    SplitOperation split = (SplitOperation) op;

	    List<Operation> ops = new Vector<Operation>();
	    ops.add(split.getFirst());
	    ops.add(split.getSecond());

	    xml += "<" + RequestPacketExtension.SPLIT_OP + ">";
	    for (Operation o : ops) {
		if (o instanceof InsertOperation) {
		    xml += insertOp(o);
		}
		if (o instanceof DeleteOperation) {
		    xml += deleteOp(o);
		}
		if (o instanceof NoOperation) {
		    xml += "<" + RequestPacketExtension.NO_OP + "/>";
		}
	    }
	    xml += "</" + RequestPacketExtension.SPLIT_OP + ">";
	}
	return xml;
    }

    private String insertOp(Operation op) {
	String xml = "";
	InsertOperation ins = (InsertOperation) op;
	xml += "<" + RequestPacketExtension.INSERT_OP + " "
		+ RequestPacketExtension.POSITION + "=\"" + ins.getPosition()
		+ "\"" + " " + RequestPacketExtension.ORIGIN + "=\""
		+ ins.getOrigin() + "\"" + ">";
	xml += "<![CDATA[" + ins.getText() + "]]>";
	xml += "</" + RequestPacketExtension.INSERT_OP + ">";
	return xml;
    }

    private String deleteOp(Operation op) {
	String xml = "";
	DeleteOperation del = (DeleteOperation) op;
	xml += "<" + RequestPacketExtension.DELETE_OP + " "
		+ RequestPacketExtension.POSITION + "=\"" + del.getPosition()
		+ "\"" + ">";
	xml += "<![CDATA[" + del.getText() + "]]>";
	xml += "</" + RequestPacketExtension.DELETE_OP + ">";
	return xml;
    }

}
