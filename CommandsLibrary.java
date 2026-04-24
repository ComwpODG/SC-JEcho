import java.util.*;

public class CommandsLibrary {

    private static Map<String, CommandHandler> commands = new HashMap<>();

    public static void registerCommand(String keyword, CommandHandler handler) {
        commands.put(keyword.toLowerCase(), handler);
    }

    public static boolean tryExecuteCommand(String message) {
        message = message.substring(message.indexOf("] ")+2);
        if (!message.startsWith("!")) return false;

        String[] parts = message.substring(1).split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        CommandHandler handler = commands.get(cmd);
        if (handler != null) {
            handler.execute(args);
            return true;
        }
        return false; // Not a known command
    }
}
