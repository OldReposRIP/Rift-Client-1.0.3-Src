package live.rift.module.modules.movement;

import live.rift.RiftMod;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Step extends Module {

    public Setting t = new Setting("Ticks", this, 25.0D, 10.0D, 30.0D, true);
    public Setting d = new Setting("Autodisable", this, false);
    private final double[] futurePositions = new double[] { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D};
    private double[] selectedPositions = new double[0];
    private int packets;

    public Step() {
        super("Step", 0, Category.MOVEMENT);
    }

    public void onUpdate() {
        if (this.check()) {
            this.selectedPositions = this.futurePositions;
            if (Step.mc.player.collidedHorizontally && Step.mc.player.onGround) {
                ++this.packets;
            }

            AxisAlignedBB bb = Step.mc.player.getEntityBoundingBox();

            int z;

            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); ++x) {
                for (z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); ++z) {
                    Block block = Step.mc.world.getBlockState(new BlockPos((double) x, bb.maxY + 1.0D, (double) z)).getBlock();
                    Block position = Step.mc.world.getBlockState(new BlockPos((double) x, bb.maxY + 2.0D, (double) z)).getBlock();
                    Block block3 = Step.mc.world.getBlockState(new BlockPos(Step.mc.player.getPositionVector().x, Step.mc.player.getPositionVector().y + 3.0D, Step.mc.player.getPositionVector().z)).getBlock();

                    if (!(block instanceof BlockAir) || !(position instanceof BlockAir) || !(block3 instanceof BlockAir)) {
                        return;
                    }
                }
            }

            if (Step.mc.player.onGround && !Step.mc.player.isInsideOfMaterial(Material.WATER) && !Step.mc.player.isInsideOfMaterial(Material.LAVA) && Step.mc.player.collidedVertically && Step.mc.player.fallDistance == 0.0F && !Step.mc.gameSettings.keyBindJump.isPressed() && Step.mc.player.collidedHorizontally && !Step.mc.player.isOnLadder() && (this.packets > this.selectedPositions.length - 2 || (double) this.packets > this.t.getValDouble())) {
                double[] adouble = this.selectedPositions;

                z = adouble.length;

                for (int i = 0; i < z; ++i) {
                    double d0 = adouble[i];

                    Step.mc.player.connection.sendPacket(new Position(Step.mc.player.posX, Step.mc.player.posY + d0, Step.mc.player.posZ, true));
                }

                Step.mc.player.setPosition(Step.mc.player.posX, Step.mc.player.posY + this.selectedPositions[this.selectedPositions.length - 1], Step.mc.player.posZ);
                this.packets = 0;
                if (this.d.getValBoolean()) {
                    this.disable();
                }
            }

        }
    }

    public boolean check() {
        return !RiftMod.fevents.moduleManager.getModule("Speed").isEnabled() && !Step.mc.player.isInWeb && !Step.mc.player.isInsideOfMaterial(Material.WATER) && !Step.mc.player.isInsideOfMaterial(Material.LAVA);
    }
}
