package app;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws NumberFormatException, IOException {
        for (String arg : args) {
            System.out.println("Argument: " + arg);
        }

        if (args.length == 0) {
            System.out.println("Not enough arguments -> exit");
        }

        switch (args[0]) {
            case "srv":
                startServer(Integer.parseInt(args[1]));
                break;
            case "sh":
                startShell();
                break;
            default:
                System.out.println("Invalid mode -> exit");
                break;
        }

    }

    private static void startShell() throws IOException {

        Shell shell = new Shell();
        shell.start();
    }

    private static void startServer(int port) throws IOException {

        Server server = new Server();
        server.start(port);
    }
}