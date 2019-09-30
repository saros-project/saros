package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {  
    public void start(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("waiting for client on port " + port + "...");
        Socket client = listen(serverSocket);

        while (true) {
            System.out.println("awaiting message");
            String nachricht = read(client);
            System.out.println(nachricht);
            System.out.println("writing answer");
            write(client, nachricht);
        }
    }

    private Socket listen(ServerSocket serverSocket) throws IOException {
        Socket socket = serverSocket.accept();
        return socket;
    }

    private String read(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        char[] buffer = new char[200];
        int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
        String nachricht = new String(buffer, 0, anzahlZeichen);
        return nachricht;
    }

    private void write(Socket socket, String message) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        if (message.length() > 0) {
            printWriter.print("need coffee...");
        }

        printWriter.flush();
    }
}