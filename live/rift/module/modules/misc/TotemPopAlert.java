package live.rift.module.modules.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Predicate;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;

public class TotemPopAlert extends Module {

    Setting mPrefix = new Setting("Prefix", this, true);
    Setting resetLog = new Setting("LogReset", this, true);
    HashMap popped = new HashMap();
    @EventHandler
    public Listener popEvent = new Listener((event) -> {
        // $FF: Couldn't be decompiled
    }, new Predicate[0]);
    @EventHandler
    public Listener joinEvent = new Listener((event) -> {
        // $FF: Couldn't be decompiled
    }, new Predicate[0]);

    public TotemPopAlert() {
        super("TotemPopAlert", 0, Category.MISC);
    }

    public void onEnable() {
        this.popped.clear();
    }

    public void onUpdate() {
        if (TotemPopAlert.mc != null && TotemPopAlert.mc.world != null && TotemPopAlert.mc.world.playerEntities != null && TotemPopAlert.mc.world.loadedEntityList != null) {
            Iterator iterator = TotemPopAlert.mc.world.playerEntities.iterator();

            while (iterator.hasNext()) {
                EntityPlayer player = (EntityPlayer) iterator.next();

                if (player.getHealth() <= 0.0F && this.popped.containsKey(player.getName())) {
                    this.sendMessage("&3" + player.getName() + " &4died after they popped &6" + this.popped.get(player.getName()) + " totems");
                    this.popped.remove(player.getName());
                }
            }

        }
    }

    public void onDisable() {
        this.popped.clear();
    }

    private void sendMessage(String msg) {
        if (this.mPrefix.getValBoolean()) {
            Messages.sendChatMessage(msg);
        } else {
            Messages.sendMessage(msg);
        }

    }
}
