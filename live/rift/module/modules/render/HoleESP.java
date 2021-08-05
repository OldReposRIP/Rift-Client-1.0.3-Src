package live.rift.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import live.rift.event.events.RenderEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.modules.combat.AutoCrystal;
import live.rift.setting.Setting;
import live.rift.util.RiftRenderer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class HoleESP extends Module {

    private final BlockPos[] surroundOffset = new BlockPos[] { new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)};
    public Setting renderDist = new Setting("Distance", this, 10.0D, 1.0D, 20.0D, false);
    public Setting a0 = new Setting("Opacity", this, 75.0D, 5.0D, 255.0D, false);
    public Setting r1 = new Setting("Obi Red", this, 255.0D, 5.0D, 255.0D, true);
    public Setting g1 = new Setting("Obi Green", this, 255.0D, 5.0D, 255.0D, true);
    public Setting b1 = new Setting("Obi Blue", this, 255.0D, 5.0D, 255.0D, true);
    public Setting r2 = new Setting("Bedrock R", this, 255.0D, 5.0D, 255.0D, true);
    public Setting g2 = new Setting("Bedrock G", this, 255.0D, 5.0D, 255.0D, true);
    public Setting b2 = new Setting("Bedrock B", this, 255.0D, 5.0D, 255.0D, true);
    public Setting mode;
    public Setting width = new Setting("Width", this, 1.0D, 1.0D, 10.0D, false);
    ArrayList options = new ArrayList();
    private ConcurrentHashMap safeHoles;

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleESP.mc.player.posX), Math.floor(HoleESP.mc.player.posY), Math.floor(HoleESP.mc.player.posZ));
    }

    public HoleESP() {
        super("HoleESP", 0, Category.RENDER);
        this.options.add("Full");
        this.options.add("Down");
        this.options.add("Outline");
        this.options.add("Half");
        this.options.add("Flat");
        this.mode = new Setting("Mode", this, "Full", this.options);
    }

    public void onUpdate() {
        if (this.safeHoles == null) {
            this.safeHoles = new ConcurrentHashMap();
        } else {
            this.safeHoles.clear();
        }

        int range = (int) Math.ceil(this.renderDist.getValDouble());
        List blockPosList = AutoCrystal.getSphere(getPlayerPos(), (float) range, range, false, true, 0);
        Iterator iterator = blockPosList.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = (BlockPos) iterator.next();

            if (HoleESP.mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && HoleESP.mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && HoleESP.mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                boolean isSafe = true;
                boolean isBedrock = true;
                BlockPos[] ablockpos = this.surroundOffset;
                int i = ablockpos.length;
                int j = 0;

                while (true) {
                    if (j < i) {
                        BlockPos offset = ablockpos[j];
                        Block block = HoleESP.mc.world.getBlockState(pos.add(offset)).getBlock();

                        if (block != Blocks.BEDROCK) {
                            isBedrock = false;
                        }

                        if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL) {
                            ++j;
                            continue;
                        }

                        isSafe = false;
                    }

                    if (isSafe) {
                        this.safeHoles.put(pos, Boolean.valueOf(isBedrock));
                    }
                    break;
                }
            }
        }

    }

    public void onWorld(RenderEvent event) {
        if (HoleESP.mc.player != null && this.safeHoles != null) {
            if (!this.safeHoles.isEmpty()) {
                this.safeHoles.forEach(accept<invokedynamic>(this));
            }
        }
    }

    private void drawBox(BlockPos blockPos, int r, int g, int b) {
        Color color = new Color(r, g, b, (int) this.a0.getValDouble());

        if (this.mode.getValString().equals("Down")) {
            RiftRenderer.prepare(7);
            RiftRenderer.drawBox(blockPos, color.getRGB(), 1);
            RiftRenderer.release();
        } else if (this.mode.getValString().equals("Full")) {
            RiftRenderer.prepare(7);
            RiftRenderer.drawBox(blockPos, color.getRGB(), 63);
            RiftRenderer.release();
        } else if (this.mode.getValString().equals("Outline")) {
            RiftRenderer.prepare(7);
            RiftRenderer.drawBoundingBoxBottomBlockPos(blockPos, (float) this.width.getValDouble(), r, g, b, (int) this.a0.getValDouble());
            RiftRenderer.release();
        } else if (this.mode.getValString().equals("Half")) {
            RiftRenderer.prepare(7);
            RiftRenderer.drawHalfBox(blockPos, r, g, b, (int) this.a0.getValDouble(), 63);
            RiftRenderer.release();
        } else if (this.mode.getValString().equals("Flat")) {
            RiftRenderer.prepare(7);
            RiftRenderer.drawOTwoBox(blockPos, r, g, b, (int) this.a0.getValDouble(), 63);
            RiftRenderer.release();
            RiftRenderer.prepare(7);
            RiftRenderer.drawOTwoBoundingBoxBlockPos(blockPos, (float) this.width.getValDouble(), r, g, b, 255);
            RiftRenderer.release();
        }

    }

    private void lambda$onWorld$0(BlockPos blockPos, Boolean isBedrock) {
        if (isBedrock.booleanValue()) {
            this.drawBox(blockPos, (int) this.r2.getValDouble(), (int) this.g2.getValDouble(), (int) this.b2.getValDouble());
        } else {
            this.drawBox(blockPos, (int) this.r1.getValDouble(), (int) this.g1.getValDouble(), (int) this.b1.getValDouble());
        }

    }

    private static enum RenderBlocks {

        OBBY, BEDROCK, BOTH;
    }

    private static enum RenderMode {

        DOWN, BLOCK;
    }
}
