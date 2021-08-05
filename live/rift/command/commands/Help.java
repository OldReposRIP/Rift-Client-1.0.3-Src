package live.rift.command.commands;

import java.util.Iterator;
import java.util.List;
import live.rift.RiftMod;
import live.rift.command.Command;
import live.rift.message.Messages;

public class Help extends Command {

    public Help() {
        this.cmd = "help";
        this.desc = "View a list of commands.";
    }

    public void handleCommand(String msg, List args) {
        Messages.sendChatMessage("List of available commands in Rift:");
        Iterator iterator = RiftMod.cmdmanager.cmds.iterator();

        while (iterator.hasNext()) {
            Command c = (Command) iterator.next();

            Messages.sendMessage("&f" + c.cmd + " &7- " + c.desc);
        }

    }
}
