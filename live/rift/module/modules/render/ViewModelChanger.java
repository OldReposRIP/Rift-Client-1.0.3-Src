package live.rift.module.modules.render;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.item.ItemSword;

public class ViewModelChanger extends Module {

    public Setting lx = new Setting("Offset X", this, 360.0D, 0.0D, 720.0D, true);
    public Setting ly = new Setting("Offset Y", this, 360.0D, 0.0D, 720.0D, true);
    public Setting lz = new Setting("Offset Z", this, 360.0D, 0.0D, 720.0D, true);
    public Setting block;
    public static ViewModelChanger i = new ViewModelChanger();

    public ViewModelChanger() {
        super("ViewModelChanger", 0, Category.RENDER);
        ViewModelChanger.i = this;
    }

    public void onUpdate() {
        if (ViewModelChanger.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && ViewModelChanger.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            ;
        }

    }
}
