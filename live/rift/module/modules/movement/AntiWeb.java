package live.rift.module.modules.movement;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;

public class AntiWeb extends Module {

    public Setting speed = new Setting("Speed Modifier", this, 4.0D, 1.0D, 10.0D, false);

    public AntiWeb() {
        super("AntiWeb", 0, Category.MOVEMENT);
    }

    public void onUpdate() {
        if (AntiWeb.mc.player.isInWeb && AntiWeb.mc.player.motionY < 0.0D) {
            AntiWeb.mc.player.motionY *= this.speed.getValDouble();
        }

    }
}
