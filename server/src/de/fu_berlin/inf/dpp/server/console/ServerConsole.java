package de.fu_berlin.inf.dpp.server.console;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Console implementation for the SarosServer.
 *
 * <p>Dynamically handles user commands and executes registered {@link ConsoleCommand}s.
 */
public class ServerConsole implements Runnable {
  private Scanner input;
  private PrintStream output;
  private List<ConsoleCommand> commands;

  public ServerConsole(InputStream in, OutputStream out) {
    input = new Scanner(in);
    output = new PrintStream(out);
    commands = new ArrayList<>();
  }

  public void registerCommand(ConsoleCommand command) {
    commands.add(command);
  }

  @Override
  public void run() {
    output.println("# Welcome to Saros Server (type 'help' for available commands)");
    while (true) {
      output.print("> ");
      output.flush();
      String line = input.nextLine().trim();

      if (line.startsWith("#")) {
        continue;
      }

      if (line.equalsIgnoreCase("quit")) {
        break;
      }

      if (line.equalsIgnoreCase("help")) {
        for (ConsoleCommand command : commands) {
          output.println(command.help());
        }
        output.println("help - Print this help");
        output.println("quit - Quit Saros Server");
      } else {
        for (ConsoleCommand command : commands) {
          List<String> cmd = Arrays.asList(line.split(" "));
          if (command.identifier().equals(cmd.get(0))) {
            command.execute(cmd.subList(1, cmd.size()), output);
            break;
          }
        }
        output.printf("Command '%s' is not known. (Type 'help' for available commands)", line);
      }
      output.flush();
    }
  }
}
