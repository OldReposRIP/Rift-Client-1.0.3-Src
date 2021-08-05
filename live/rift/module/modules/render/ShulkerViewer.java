package live.rift.module.modules.render;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;

public class ShulkerViewer extends Module {

    public Setting opacity = new Setting("Opacity", this, 200.0D, 5.0D, 255.0D, true);

    public ShulkerViewer() {
        super("ShulkerViewer", 0, Category.RENDER);
    }
}
