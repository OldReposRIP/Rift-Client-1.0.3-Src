package live.rift.module.modules.hud;

import live.rift.RiftMod;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;

public class GUI extends Module {

    public Setting red = new Setting("Red", this, 150.0D, 10.0D, 255.0D, true);
    public Setting green = new Setting("Green", this, 150.0D, 10.0D, 255.0D, true);
    public Setting blue = new Setting("Blue", this, 150.0D, 10.0D, 255.0D, true);
    public Setting rainbow = new Setting("Rainbow", this, false);

    public GUI() {
        super("Gui", 205, Category.HUD);
    }

    public void onToggle(boolean state) {
        GUI.mc.displayGuiScreen(RiftMod.clickgui);
    }
}
