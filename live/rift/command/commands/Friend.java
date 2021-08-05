package live.rift.command.commands;

import java.util.List;
import live.rift.RiftMod;
import live.rift.command.Command;
import live.rift.friends.Friends;
import live.rift.message.Messages;

public class Friend extends Command {

    public Friend() {
        this.cmd = "friend";
        this.aliases.add("f");
        this.aliases.add("fr");
        this.aliases.add("friends");
        this.desc = "Change the friends list";
    }

    public void handleCommand(String msg, List args) {
        if (!this.argExists(args, 1)) {
            Messages.sendChatMessage("Friends command: ");
            Messages.sendMessage("friend <username/add/del> <player>");
        } else if (!this.argExists(args, 2) && !((String) args.get(1)).equalsIgnoreCase("add") && !((String) args.get(1)).equalsIgnoreCase("del")) {
            Messages.sendChatMessage(Friends.isFriend((String) args.get(1)) ? "&a" + (String) args.get(1) + "&f is friended." : "&c" + (String) args.get(1) + "&f is not friended.");
        } else if (this.argExists(args, 2)) {
            Friends friends;

            if (((String) args.get(1)).equalsIgnoreCase("add")) {
                if (!Friends.isFriend((String) args.get(2))) {
                    friends = RiftMod.friends;
                    Friends.addFriend((String) args.get(2));
                    Messages.sendChatMessage("&aAdded " + (String) args.get(2) + " to the friendslist.");
                } else {
                    Messages.sendChatMessage("&a" + (String) args.get(2) + " is already on the friendslist!");
                }
            }

            if (((String) args.get(1)).equalsIgnoreCase("del")) {
                if (Friends.isFriend((String) args.get(2))) {
                    friends = RiftMod.friends;
                    Friends.removeFriend((String) args.get(2));
                    Messages.sendChatMessage("&cRemoved " + (String) args.get(2) + " from the friendslist.");
                } else {
                    Messages.sendChatMessage("&c" + (String) args.get(2) + " isn\'t on the friendslist!");
                }
            }
        } else {
            Messages.sendChatMessage("&cYou must specify a user to remove from the friendslist!");
        }

    }
}
