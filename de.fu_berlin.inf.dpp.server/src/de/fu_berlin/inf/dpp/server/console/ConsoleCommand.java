package de.fu_berlin.inf.dpp.server.console;

import java.io.PrintStream;

/**
 * A command that can be registered to the {@link ServerConsole}
 * and then called by the user.
 */
public abstract class ConsoleCommand {
    public abstract String identifier();

    public abstract String help();

    public boolean matches(String command) {
        return command.split(" ")[0].equals(identifier());
    }

    public abstract void execute(String command, PrintStream out);
}
