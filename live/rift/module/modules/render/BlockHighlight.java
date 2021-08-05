package live.rift.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import live.rift.RiftMod;
import live.rift.event.events.RenderEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.RainbowUtil;
import live.rift.util.RiftRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class BlockHighlight extends Module {

    public Setting a = new Setting("Opacity", this, 75.0D, 5.0D, 255.0D, true);
    public Setting width = new Setting("Width", this, 1.0D, 0.0D, 10.0D, true);
    public Setting mode;
    ArrayList options = new ArrayList();
    RainbowUtil rutil = new RainbowUtil(9);

    public BlockHighlight() {
        super("BlockHighlight", 0, Category.RENDER);
        this.options.add("Face");
        this.options.add("Outline");
        this.mode = new Setting("Mode", this, "Face", this.options);
    }

    public void onWorld(RenderEvent event) {
        byte ri = 0;

        this.rutil.onRender();
        if (ri >= 355) {
            ri = 0;
        }

        int rgb = this.getColor(ri);
        float r = (float) (rgb >> 16 & 255);
        float g = (float) (rgb >> 8 & 255);
        float b = (float) (rgb & 255);
        RayTraceResult ray = BlockHighlight.mc.objectMouseOver;

        if (ray != null && ray.typeOfHit == Type.BLOCK && BlockHighlight.mc.world != null) {
            BlockPos bp = ray.getBlockPos();
            AxisAlignedBB bb = BlockHighlight.mc.world.getBlockState(bp).getSelectedBoundingBox(BlockHighlight.mc.world, bp);

            if (bb != null && bp != null && BlockHighlight.mc.world.getBlockState(bp).getMaterial() != Material.AIR) {
                RiftRenderer.prepare(7);
                this.drawBox(bp, (int) r, (int) g, (int) b, bb);
                RiftRenderer.release();
            }
        }

    }

    private void drawBox(BlockPos blockPos, int r, int g, int b, AxisAlignedBB bb) {
        RayTraceResult ray = BlockHighlight.mc.objectMouseOver;

        if (ray != null && ray.typeOfHit == Type.BLOCK && BlockHighlight.mc.world != null) {
            Color color = new Color(r, g, b, (int) this.a.getValDouble());

            if (this.mode.getValString().equals("Face")) {
                if (ray.sideHit == EnumFacing.DOWN) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 1);
                }

                if (ray.sideHit == EnumFacing.UP) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 2);
                }

                if (ray.sideHit == EnumFacing.WEST) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 16);
                }

                if (ray.sideHit == EnumFacing.EAST) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 32);
                }

                if (ray.sideHit == EnumFacing.NORTH) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 4);
                }

                if (ray.sideHit == EnumFacing.SOUTH) {
                    RiftRenderer.drawBox(blockPos, color.getRGB(), 8);
                }
            } else if (this.mode.getValString().equals("Outline")) {
                double x = bb.minX;
                double y = bb.minY;
                double z = bb.minZ;
                double w = bb.maxX - bb.minX;
                double h = bb.maxY - bb.minY;
                double l = bb.maxZ - bb.minZ;

                RiftRenderer.drawBoundingBox(x, y, z, w, l, h, this.width.getValFloat(), r, g, b, 255);
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
