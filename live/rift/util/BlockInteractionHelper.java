package live.rift.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockInteractionHelper {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static final List blackList = Arrays.asList(new Block[] { Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR, Blocks.ENCHANTING_TABLE});
    public static final List shulkerList = Arrays.asList(new Block[] { Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX});

    public static void placeBlockScaffold(BlockPos pos, Integer delay) {
        Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + (double) Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
        EnumFacing[] aenumfacing = EnumFacing.values();
        int i = aenumfacing.length;

        for (int j = 0; j < i; ++j) {
            EnumFacing side = aenumfacing[j];
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();

            if (canBeClicked(neighbor)) {
                Vec3d hitVec = (new Vec3d(neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));

                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625D) {
                    faceVectorPacketInstant(hitVec);
                    if (BlockInteractionHelper.mc.player.ticksExisted % delay.intValue() == 0) {
                        processRightClickBlock(neighbor, side2, hitVec);
                        Wrapper.getPlayer().swingArm(EnumHand.MAIN_HAND);
                        BlockInteractionHelper.mc.rightClickDelayTimer = 4;
                    }

                    return;
                }
            }
        }

    }

    private static void processRightClickBlock(BlockPos pos, EnumFacing side, Vec3d hitVec) {
        getPlayerController().processRightClickBlock(Wrapper.getPlayer(), BlockInteractionHelper.mc.world, pos, side, hitVec, EnumHand.MAIN_HAND);
    }

    private static PlayerControllerMP getPlayerController() {
        return Minecraft.getMinecraft().playerController;
    }

    public ArrayList GetPlaceableSides(BlockPos b) {
        ArrayList placeableSides = new ArrayList();
        BlockPos[] offset = new BlockPos[] { new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 1, 0), new BlockPos(0, -1, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
        BlockPos[] ablockpos = offset;
        int i = offset.length;

        for (int j = 0; j < i; ++j) {
            BlockPos o = ablockpos[j];
            BlockPos c = b.add(o.x, o.y, o.z);

            if (BlockInteractionHelper.mc.world.getBlockState(c).getBlock().isReplaceable(BlockInteractionHelper.mc.world, c)) {
                if (o.x == 1) {
                    placeableSides.add(EnumFacing.EAST);
                }

                if (o.x == -1) {
                    placeableSides.add(EnumFacing.WEST);
                }

                if (o.y == 1) {
                    placeableSides.add(EnumFacing.UP);
                }

                if (o.y == -1) {
                    placeableSides.add(EnumFacing.DOWN);
                }

                if (o.z == 1) {
                    placeableSides.add(EnumFacing.SOUTH);
                }

                if (o.z == -1) {
                    placeableSides.add(EnumFacing.NORTH);
                }
            }
        }

        return placeableSides;
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);

        BlockInteractionHelper.mc.player.connection.sendPacket(new Rotation(rotations[0], rotations[1], BlockInteractionHelper.mc.player.onGround));
    }

    private static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));

        return new float[] { BlockInteractionHelper.mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - BlockInteractionHelper.mc.player.rotationYaw), BlockInteractionHelper.mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - BlockInteractionHelper.mc.player.rotationPitch)};
    }

    private static Vec3d getEyesPos() {
        return new Vec3d(BlockInteractionHelper.mc.player.posX, BlockInteractionHelper.mc.player.posY + (double) BlockInteractionHelper.mc.player.getEyeHeight(), BlockInteractionHelper.mc.player.posZ);
    }

    public static boolean canBeClicked(BlockPos pos) {
        return getBlock(pos).canCollideCheck(getState(pos), false);
    }

    private static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    private static IBlockState getState(BlockPos pos) {
        return BlockInteractionHelper.mc.world.getBlockState(pos);
    }
}
