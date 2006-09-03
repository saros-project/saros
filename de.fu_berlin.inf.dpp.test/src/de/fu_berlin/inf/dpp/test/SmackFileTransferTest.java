package de.fu_berlin.inf.dpp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

public class SmackFileTransferTest {
    static {
        XMPPConnection.DEBUG_ENABLED = true;
    }
    
    private XMPPConnection connection1;
    private FileTransferManager transferManager1;
    
    private XMPPConnection connection2;
    private FileTransferManager transferManager2;
    
    protected void setUp() throws Exception {
        connection1 = new XMPPConnection("jabber.org");
        while (!connection1.isAuthenticated()) {
            System.out.println("connecting user1");
            connection1.login("saros1", "greentea");
        }
        transferManager1 = new FileTransferManager(connection1);
        
        connection2 = new XMPPConnection("jabber.org");
        while (!connection2.isAuthenticated()) {
            System.out.println("connecting user2");            
            connection2.login("saros3", "greentea");
        }
        transferManager2 = new FileTransferManager(connection2);
    }
    public void testSingleFileTransfer() throws XMPPException, IOException {
        System.out.println("=== testSingleFileTransfer ===");
        
        transferManager2.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest request) {
                receiveText(request);
            }
        });
        
        sendText(transferManager1, "saros3@jabber.org/Smack", "HEHE");
    }
    
    public void testViceVersaFileTransfer() throws XMPPException, IOException {
        System.out.println("=== testViceVersaFileTransfer ===");
        
        transferManager2.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest request) {
                try {
                    receiveText(request);
                    sendText(transferManager2, "saros1@jabber.org/Smack", "TETE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        transferManager1.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest request) {
                try {
                    receiveText(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        sendText(transferManager1, "saros3@jabber.org/Smack", "HEHE");
    }
    
    public void testConcurrentFileTransfers() throws XMPPException, IOException {
        System.out.println("=== testViceVersaFileTransfer ===");
        
        transferManager2.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest request) {
                try {
                    receiveText(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE1");
        asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE2");
        asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE3");
    }
    
    public static void main(String[] args) {
        try {
            SmackFileTransferTest test = new SmackFileTransferTest();
            test.setUp();
            test.testConcurrentFileTransfers();
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void sendText(FileTransferManager transferManager, String to, 
        String text) throws XMPPException, IOException {
        
        System.out.println("Sending text("+text+") to "+to);
        
        OutgoingFileTransfer transfer = 
            transferManager.createOutgoingFileTransfer(to);
        
        OutputStream out = transfer.sendFile("test file", 
            text.getBytes().length, "test desc");

        BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
        writer.write(text);
        writer.flush();
        writer.close();
        
        System.out.println("Sent text("+text+") to "+to);
    }
    
    private String receiveText(FileTransferRequest request) {
        String text = null;
        
        System.out.println("Receiving text from "+request.getRequestor());
        
        final IncomingFileTransfer transfer = request.accept();
        
        try {
            InputStream in = transfer.recieveFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            
            try {
                String line = null;
                
                while((line=reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch(Throwable e) {
                e.printStackTrace();
                
            } finally {
                reader.close();
            }   
            
            text = sb.toString();
            
            System.out.println("Received text("+text+") from "+request.getRequestor());
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return text;
    }

    private void asyncSendText(final FileTransferManager transferManager, 
        final String to, final String text) {
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendText(transferManager, to, text);
                } catch (XMPPException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
