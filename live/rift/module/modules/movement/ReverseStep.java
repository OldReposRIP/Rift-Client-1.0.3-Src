package live.rift.module.modules.movement;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.init.Blocks;

public class ReverseStep extends Module {

    public Setting height;
    boolean wasOnGround;
    boolean isStepInProgress;

    public ReverseStep() {
        super("Reverse Step", 0, Category.MOVEMENT);
    }

    public void onUpdate() {
        if (ReverseStep.mc.world != null) {
            if (this.wasOnGround && !ReverseStep.mc.player.onGround && ReverseStep.mc.player.motionY < 0.0D) {
                this.isStepInProgress = true;
            }

            if (this.isStepInProgress && !ReverseStep.mc.player.onGround && (ReverseStep.mc.world.getBlockState(ReverseStep.mc.player.getPosition().down(2)).getBlock() != Blocks.AIR || ReverseStep.mc.world.getBlockState(ReverseStep.mc.player.getPosition().down(3)).getBlock() != Blocks.AIR || ReverseStep.mc.world.getBlockState(ReverseStep.mc.player.getPosition().down(4)).getBlock() != Blocks.AIR) && ReverseStep.mc.player.motionY < 0.0D) {
                ReverseStep.mc.player.motionY = -1.0D;
                ReverseStep.mc.player.motionX *= 0.2D;
                ReverseStep.mc.player.motionZ *= 0.2D;
            }

            if (this.isStepInProgress && ReverseStep.mc.player.onGround) {
                this.isStepInProgress = false;
            }

            if (ReverseStep.mc.player.onGround) {
                this.wasOnGround = true;
            } else {
                this.wasOnGround = false;
            }

        }
    }
}
