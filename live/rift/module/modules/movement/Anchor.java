package live.rift.module.modules.movement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import live.rift.RiftMod;
import live.rift.module.Category;
import live.rift.module.Module;
import net.minecraft.block.Block;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.BlockPos;

public class Anchor extends Module {

    public BlockPos[] offset = new BlockPos[] { new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1)};
    public int sinceTP = 2;
    public boolean didTP = false;

    public Anchor() {
        super("Anchor", 0, Category.MOVEMENT);
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(Anchor.mc.player.posX), Math.floor(Anchor.mc.player.posY), Math.floor(Anchor.mc.player.posZ));
    }

    public boolean isHole(BlockPos p) {
        int blocks = 0;
        BlockPos[] ablockpos = this.offset;
        int i = ablockpos.length;

        for (int j = 0; j < i; ++j) {
            BlockPos o = ablockpos[j];
            BlockPos f = p.add(o.x, o.y, o.z);
            Block b = Anchor.mc.world.getBlockState(f).getBlock();

            if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK || b == Blocks.ENDER_CHEST) {
                ++blocks;
            }
        }

        if (blocks == 4 && Anchor.mc.world.getBlockState(p).getBlock() == Blocks.AIR) {
            return true;
        } else {
            return false;
        }
    }

    public void onUpdate() {
        if (!RiftMod.fevents.moduleManager.getModule("Step").isEnabled() && !RiftMod.fevents.moduleManager.getModule("Speed").isEnabled()) {
            if (!this.didTP) {
                BlockPos cH = (BlockPos) getSphere(getPlayerPos(), 1.4F, 3, false, true, 0).stream().filter(this::isHole).map((b) -> {
                    return b;
                }).min(Comparator.comparing((b) -> {
                    return Double.valueOf(Anchor.mc.player.getDistanceSq(b));
                })).orElse((Object) null);

                if (cH == null || Anchor.mc.player == null) {
                    return;
                }

                double xDiff = (double) cH.x + 0.45D - Anchor.mc.player.getPositionVector().x;
                double zDiff = (double) cH.z + 0.45D - Anchor.mc.player.getPositionVector().z;

                if (Math.abs(xDiff) < 0.05D || Math.abs(zDiff) < 0.05D) {
                    return;
                }

                Anchor.mc.player.move(MoverType.SELF, xDiff * 0.287D, Anchor.mc.player.motionY, zDiff * 0.28700000047683716D);
                this.didTP = true;
            }

            if (this.didTP) {
                --this.sinceTP;
            }

            if (this.sinceTP <= 0) {
                this.didTP = false;
                this.sinceTP = 2;
            }

        } else {
            this.didTP = true;
        }
    }

    public static List getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList circleblocks = new ArrayList();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();

        for (int x = cx - (int) r; (float) x <= (float) cx + r; ++x) {
            for (int z = cz - (int) r; (float) z <= (float) cz + r; ++z) {
                for (int y = sphere ? cy - (int) r : cy; (float) y < (sphere ? (float) cy + r : (float) (cy + h)); ++y) {
                    double dist = (double) ((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));

                    if (dist < (double) (r * r) && (!hollow || dist >= (double) ((r - 1.0F) * (r - 1.0F)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);

                        circleblocks.add(l);
                    }
                }
            }
        }

        return circleblocks;
    }

    private void centerPlayer(double x, double y, double z) {
        Anchor.mc.player.connection.sendPacket(new Position(x, y, z, true));
        Anchor.mc.player.setPosition(x, y, z);
    }
}
