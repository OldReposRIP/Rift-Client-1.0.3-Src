package live.rift.module.modules.render;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import live.rift.RiftMod;
import live.rift.event.events.RenderEvent;
import live.rift.friends.Friends;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.modules.combat.AutoCrystal;
import live.rift.setting.Setting;
import live.rift.util.RainbowUtil;
import live.rift.util.RiftRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class CityESP extends Module {

    public Setting red;
    public Setting green;
    public Setting blue;
    public Setting a = new Setting("Opacity", this, 75.0D, 5.0D, 255.0D, false);
    public Setting width = new Setting("Width", this, 1.0D, 0.0D, 2.0D, false);
    private final BlockPos[] surroundOffset = new BlockPos[] { new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)};
    RainbowUtil rutil = new RainbowUtil(9);

    public CityESP() {
        super("CityESP", 0, Category.RENDER);
    }

    public void onWorld(RenderEvent event) {
        byte ri = 0;

        this.rutil.onRender();
        if (ri >= 355) {
            ri = 0;
        }

        if (CityESP.mc.world != null) {
            int rgb = this.getColor(ri);
            int r = rgb >> 16 & 255;
            int g = rgb >> 8 & 255;
            int b = rgb & 255;
            AutoCrystal aa = (AutoCrystal) RiftMod.fevents.moduleManager.getModule("AutoCrystal");
            List entities = (List) CityESP.mc.world.playerEntities.stream().filter((entityPlayer) -> {
                return !Friends.isFriend(entityPlayer.getName());
            }).collect(Collectors.toList());
            Iterator iterator = entities.iterator();

            while (iterator.hasNext()) {
                EntityPlayer e = (EntityPlayer) iterator.next();
                BlockPos[] ablockpos = this.surroundOffset;
                int i = ablockpos.length;

                for (int j = 0; j < i; ++j) {
                    BlockPos add = ablockpos[j];
                    BlockPos o = (new BlockPos(e.getPositionVector().x, e.getPositionVector().y, e.getPositionVector().z)).add(add.x, add.y, add.z);

                    if (CityESP.mc.world.getBlockState(o).getBlock() == Blocks.OBSIDIAN && (aa.canPlaceCrystal(o.north(1).down()) || aa.canPlaceCrystal(o.east(1).down()) || aa.canPlaceCrystal(o.south(1).down()) || aa.canPlaceCrystal(o.west(1).down()))) {
                        RiftRenderer.prepare(7);
                        RiftRenderer.drawBoundingBoxBlockPos(o, this.width.getValFloat(), r, g, b, (int) this.a.getValDouble());
                        RiftRenderer.release();
                    }
                }
            }

        }
    }

    public int getColor(int index) {
        boolean color = true;
        int color1;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            color1 = this.rutil.GetRainbowColorAt(index);
        } else {
            color1 = (new Color((int) RiftMod.setmgr.getSettingByNameMod("Red", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Green", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Blue", "Gui").getValDouble())).getRGB();
        }

        return color1;
    }
}
