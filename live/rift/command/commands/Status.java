package live.rift.command.commands;

import java.util.Iterator;
import java.util.List;
import live.rift.command.Command;
import live.rift.message.Messages;

public class Status extends Command {

    public static String statusParsed;

    public Status() {
        this.cmd = "status";
        this.aliases.add("s");
        this.aliases.add("rpc");
    }

    public void handleCommand(String msg, List args) {
        if (args.size() <= 1) {
            Messages.sendMessage("&c Error: Status needs an argument.");
        }

        if (args.size() >= 2) {
            Status.statusParsed = "";
            Iterator iterator = args.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                if (s != args.get(0)) {
                    Status.statusParsed = Status.statusParsed + " " + s;
                }
            }

            Messages.sendMessage("Updated RPC status to " + Status.statusParsed);
        }

    }
}
