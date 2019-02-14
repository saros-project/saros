package de.fu_berlin.inf.dpp.server.console;

import java.io.PrintStream;
import java.util.List;

/** A command that can be registered to the {@link ServerConsole} and then called by the user. */
public abstract class ConsoleCommand {
  public abstract String identifier();

  public abstract String help();

  public abstract void execute(List<String> args, PrintStream out);
}
