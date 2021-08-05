package live.rift.module.modules.misc;

import java.util.ArrayList;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.util.math.RayTraceResult.Type;

public class NoEntityBlock extends Module {

    static NoEntityBlock INST;
    public Setting mode;
    public ArrayList modes = new ArrayList();

    public NoEntityBlock() {
        super("NoEntityTrace", 0, Category.MISC);
        NoEntityBlock.INST = this;
        this.modes.add("Normal");
        this.modes.add("Dev");
        this.mode = new Setting("Mode", this, "Normal", this.modes);
    }

    public static boolean doBlock() {
        return NoEntityBlock.INST != null ? (!NoEntityBlock.INST.mode.getValString().equalsIgnoreCase("Normal") ? false : (NoEntityBlock.mc.objectMouseOver != null ? NoEntityBlock.INST.isEnabled() && NoEntityBlock.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && NoEntityBlock.mc.objectMouseOver.getBlockPos() != null : (!NoEntityBlock.INST.mode.getValString().equalsIgnoreCase("Dev") ? false : (NoEntityBlock.mc.objectMouseOver != null ? (NoEntityBlock.mc.objectMouseOver.typeOfHit == null ? false : NoEntityBlock.INST.isEnabled() && NoEntityBlock.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && NoEntityBlock.mc.objectMouseOver.typeOfHit == Type.BLOCK) : false)))) : false;
    }
}
