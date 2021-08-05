package live.rift.module.modules.misc;

import java.util.function.Predicate;
import live.rift.RiftMod;
import live.rift.friends.Friends;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;

public class MCF extends Module {

    @EventHandler
    private Listener middleClick = new Listener((event) -> {
        if (MCF.mc.objectMouseOver != null && MCF.mc.objectMouseOver.entityHit instanceof EntityPlayer) {
            Friends friends;

            if (!Friends.isFriend(MCF.mc.objectMouseOver.entityHit.getName())) {
                friends = RiftMod.friends;
                Friends.addFriend(MCF.mc.objectMouseOver.entityHit.getName());
                Messages.sendChatMessage("&aAdded " + MCF.mc.objectMouseOver.entityHit.getName() + " to the friendslist.");
            } else {
                friends = RiftMod.friends;
                Friends.removeFriend(MCF.mc.objectMouseOver.entityHit.getName());
                Messages.sendChatMessage("&cRemoved " + MCF.mc.objectMouseOver.entityHit.getName() + " from the friendslist.");
            }
        }

    }, new Predicate[0]);

    public MCF() {
        super("MiddleClick", 0, Category.MISC);
    }
}
