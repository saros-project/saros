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


public class RequestPacketExtension  implements PacketExtension{
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
	
	
	public static final String LOCAL_TIME = "localtime";
	
	public static final String REMOTE_TIME = "remotetime";
	
	public static final String POSITION = "position";
	
	public static final String ORIGIN = "origin";
	
	public static final String TEXT = "text";
	
	public static final String LENGTH = "length";
	
	
	
	private Request request;
	
	public RequestPacketExtension(Request request){
		this.request = request;
	}
	
	public void setRequest(Request request){
		this.request = request;
	}
	
	public Request getRequest(){
		return request;
	}
	
	public String getElementName() {
		return ELEMENT;
	}

	public String getNamespace() {
		return NAMESPACE;
	}

	public String toXML() {
		if(request == null){
			return "";
		}

		StringBuffer buf = new StringBuffer();
		buf.append("<").append(getElementName());
		buf.append(" xmlns=\"").append(getNamespace()+"\"");
		
//		buf.append(" xmlns=\"").append(getNamespace()).append("\">");
		
		buf.append(">");
		
		buf.append(pathToXML());
		buf.append(jidToXML());
		buf.append(sideIDToXML());
		buf.append(vectorTimeToXML());
		buf.append(operationToXML());
		
//		buf.append(requestToXML());

		buf.append("</").append(getElementName()).append(">");
		return buf.toString();
//		return "<request></request>";
	}
	
	private String pathToXML(){
		return "<"+PATH+">"+ request.getEditorPath()+"</"+PATH+">";
	}
	
	private String jidToXML(){
		return "<"+JID+">"+ request.getJID()+"</"+JID+">";
	}
	
	private String sideIDToXML(){
		return "<"+SIDE_ID+">"+ request.getSiteId()+"</"+SIDE_ID+">";
	}
	
	private String vectorTimeToXML(){
		String xml = "";
		Timestamp timestamp = request.getTimestamp();
		xml += "<"+VECTOR_TIME + " local=\""+timestamp.getComponents()[0]+"\" remote=\""+timestamp.getComponents()[1]+"\""+ "/>";
		return xml;
	}
	
	private String operationToXML(){
		Operation op = request.getOperation();
		String xml = "";
		if(op instanceof InsertOperation){
			xml += insertOp(op);
		}
		if(op instanceof DeleteOperation){
			xml += deleteOp(op);
		}
		if(op instanceof NoOperation){
//			NoOperation no = (NoOperation) op;
			xml += "<"+NO_OP+ "/>";
		}
		if(op instanceof SplitOperation){
			SplitOperation split = (SplitOperation) op;
			
			List<Operation> ops = new Vector<Operation>();
			ops.add(split.getFirst());
			ops.add(split.getSecond());
			
			xml += "<"+SPLIT_OP+">";
			for(Operation o: ops){
				if(o instanceof InsertOperation){
					xml += insertOp(o);
				}
				if(o instanceof DeleteOperation){
					xml += deleteOp(o);
				}
				if( o instanceof NoOperation){
					xml += "<"+NO_OP+ "/>";
				}
			}
			xml += "</"+SPLIT_OP+">";
		}
		return xml;
	}
	
	private String insertOp(Operation op){
		String xml = "";
		InsertOperation ins = (InsertOperation) op;
		xml += "<"+INSERT_OP+ " "+POSITION+"=\""+ins.getPosition()+"\""+ " "+ORIGIN+"=\""+ins.getOrigin()+"\""+ ">";
		xml += "<![CDATA[" + ins.getText() + "]]>";
		xml += "</"+INSERT_OP+">";
		return xml;
	}
	
	private String deleteOp(Operation op){
		String xml = "";
		DeleteOperation del = (DeleteOperation) op;
		xml += "<"+DELETE_OP+ " "+POSITION+"=\""+del.getPosition()+"\""+">";
		xml += "<![CDATA[" + del.getText() + "]]>";
		xml += "</"+DELETE_OP+">";
		return xml;
	}
	
	private String requestToXML(){
		return "<request " 
//		+ "path=\"" + request.getEditorPath() + "\">"
//		+ "path=\"" + request.getEditorPath() + "\" " +"offset=\"" + textEditActivity.offset + "\" " + "replace=\""
//		+ textEditActivity.replace + "\">" + "<![CDATA[" + textEditActivity.text + "]]>"
		
		+ "</request>";
//			return "<request " + "path=\"" + request.getEditorPath() + "\" " +"offset=\"" + textEditActivity.offset + "\" " + "replace=\""
//				+ textEditActivity.replace + "\">" + "<![CDATA[" + textEditActivity.text + "]]>"
//				+ "</request>";
	}

}
