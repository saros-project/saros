package saros;

import java.io.IOException;
import java.net.Socket;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import saros.lsp.SarosLanguageServer;

public class App {

    public static void main(String[] args) throws Exception {

        if (args.length > 1) {
            throw new IllegalArgumentException("wrong number of arguments");
        } else if (args.length != 1) {
            throw new IllegalArgumentException("port parameter not supplied");
        }

        int port = Integer.parseInt(args[0]);
        Socket socket = new Socket("localhost", port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    socket.close();
                } catch (IOException e) {
                    // NOP
                }
            } 
        }); 
        
        SarosLanguageServer langSvr = new SarosLanguageServer();
        Launcher<LanguageClient> l = LSPLauncher.createServerLauncher(langSvr, socket.getInputStream(), socket.getOutputStream());
       
        LanguageClient langClt = l.getRemoteProxy();
        langSvr.connect(langClt);

        l.startListening();

        langSvr.sendHello();
    }
}