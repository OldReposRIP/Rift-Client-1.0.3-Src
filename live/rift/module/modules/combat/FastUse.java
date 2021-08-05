package live.rift.module.modules.combat;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.init.Items;

public class FastUse extends Module {

    Setting delay = new Setting("Delay", this, 0.0D, 0.0D, 4.0D, true);

    public FastUse() {
        super("FastUse", 0, Category.COMBAT);
    }

    public void onUpdate() {
        if (FastUse.mc.world != null) {
            if (FastUse.mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || FastUse.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                FastUse.mc.rightClickDelayTimer = (int) this.delay.getValDouble();
            }

        }
    }

    public void onDisable() {
        FastUse.mc.rightClickDelayTimer = 4;
    }
}
