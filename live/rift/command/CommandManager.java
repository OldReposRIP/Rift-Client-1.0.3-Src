package live.rift.command;

import java.util.ArrayList;
import java.util.Iterator;
import live.rift.command.commands.Friend;
import live.rift.command.commands.Help;
import live.rift.command.commands.Set;
import live.rift.command.commands.Status;
import live.rift.message.Messages;

public class CommandManager {

    public ArrayList cmds = new ArrayList();

    public CommandManager() {
        this.cmds.add(new Friend());
        this.cmds.add(new Help());
        this.cmds.add(new Status());
        this.cmds.add(new Set());
    }

    public void handleCMD(String msg) {
        ArrayList args = new ArrayList();
        String[] iscmd = msg.split(" ");
        int i = iscmd.length;

        for (int c = 0; c < i; ++c) {
            String arg = iscmd[c];

            args.add(arg);
        }

        if (args.get(0) == null) {
            args.add(msg);
        }

        boolean flag = false;
        Iterator iterator = this.cmds.iterator();

        while (iterator.hasNext()) {
            Command command = (Command) iterator.next();

            if (((String) args.get(0)).equalsIgnoreCase(command.getPre()) || command.aliases.contains(((String) args.get(0)).toLowerCase())) {
                command.handleCommand((String) args.get(0), args);
                flag = true;
            }
        }

        if (!flag) {
            Messages.sendChatMessage("&cCommand not found. try \'help\' for help.");
        }

    }
}
