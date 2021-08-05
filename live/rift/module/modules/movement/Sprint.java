package live.rift.module.modules.movement;

import java.util.ArrayList;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraftforge.common.MinecraftForge;

public class Sprint extends Module {

    public Setting mode;
    ArrayList modes = new ArrayList();

    public Sprint() {
        super("Sprint", 0, Category.MOVEMENT);
        this.modes.add("Force");
        this.modes.add("OnWalk");
        this.mode = new Setting("Mode", this, "Force", this.modes);
    }

    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void onUpdate() {
        if (this.mode.getValString().equals("Force") && (!Sprint.mc.player.isSneaking() && !Sprint.mc.player.collidedHorizontally && Sprint.mc.gameSettings.keyBindForward.isKeyDown() || Sprint.mc.gameSettings.keyBindLeft.isKeyDown() || Sprint.mc.gameSettings.keyBindRight.isKeyDown() || Sprint.mc.gameSettings.keyBindBack.isKeyDown() && (float) Sprint.mc.player.getFoodStats().getFoodLevel() > 6.0F)) {
            Sprint.mc.player.setSprinting(true);
        }

        if (this.mode.getValString().equals("OnWalk") && Sprint.mc.gameSettings.keyBindForward.isKeyDown() && !Sprint.mc.player.collidedHorizontally && !Sprint.mc.player.isSneaking() && !Sprint.mc.player.isHandActive() && (float) Sprint.mc.player.getFoodStats().getFoodLevel() > 6.0F) {
            Sprint.mc.player.setSprinting(true);
        }

    }
}
