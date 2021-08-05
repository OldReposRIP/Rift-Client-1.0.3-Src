package live.rift.module.modules.combat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import live.rift.event.events.PacketEvent;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.BlockInteractionHelper;
import live.rift.util.BlockUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class HoleFill extends Module {

    Setting range = new Setting("Range", this, 4.5D, 1.0D, 10.0D, false);
    Setting delay = new Setting("Delay", this, 4.0D, 1.0D, 10.0D, true);
    Setting autoDisable = new Setting("AutoDisable", this, true);
    int delayTick;
    int obsidianSlot;
    BlockPos render;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private Listener packetListener = new Listener((event) -> {
        Packet packet = event.getPacket();

        if (packet instanceof CPacketPlayer && HoleFill.isSpoofingAngles) {
            ((CPacketPlayer) packet).yaw = (float) HoleFill.yaw;
            ((CPacketPlayer) packet).pitch = (float) HoleFill.pitch;
        }

    }, new Predicate[0]);

    public HoleFill() {
        super("HoleFiller", 0, Category.COMBAT);
    }

    public void onEnable() {
        this.delayTick = (int) (2.0D * this.delay.getValDouble());
        this.obsidianSlot = -1;
    }

    public void onUpdate() {
        --this.delayTick;
        if (HoleFill.mc.world != null) {
            List blocks = this.findCrystalBlocks();
            BlockPos q = null;

            if (blocks.size() < 1) {
                Messages.sendChatMessage("Holefill is finished filling holes, disabling");
                this.disable();
            }

            int obsidianSlot = HoleFill.mc.player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN) ? HoleFill.mc.player.inventory.currentItem : -1;
            int oldSlot;

            if (obsidianSlot == -1) {
                for (oldSlot = 0; oldSlot < 9; ++oldSlot) {
                    if (HoleFill.mc.player.inventory.getStackInSlot(oldSlot).getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
                        obsidianSlot = oldSlot;
                        break;
                    }
                }
            }

            if (obsidianSlot != -1) {
                Iterator iterator = blocks.iterator();

                while (iterator.hasNext()) {
                    BlockPos blockPos = (BlockPos) iterator.next();

                    if (HoleFill.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) {
                        q = blockPos;
                    }
                }

                if (q != null && HoleFill.mc.player.onGround && this.delayTick < 0) {
                    this.delayTick = (int) (2.0D * this.delay.getValDouble());
                    oldSlot = HoleFill.mc.player.inventory.currentItem;
                    if (HoleFill.mc.player.inventory.currentItem != obsidianSlot) {
                        HoleFill.mc.player.inventory.currentItem = obsidianSlot;
                    }

                    this.lookAtPacket((double) q.x + 0.5D, (double) q.y - 0.5D, (double) q.z + 0.5D, HoleFill.mc.player);
                    BlockInteractionHelper.placeBlockScaffold(q, Integer.valueOf(1));
                    HoleFill.mc.player.swingArm(EnumHand.MAIN_HAND);
                    HoleFill.mc.player.inventory.currentItem = oldSlot;
                    resetRotation();
                }

            }
        }
    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = BlockUtil.calculateLookAt(px, py, pz, me);

        setYawAndPitch((float) v[0], (float) v[1]);
    }

    private boolean IsHole(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 0, 0);
        BlockPos boost3 = blockPos.add(0, 0, -1);
        BlockPos boost4 = blockPos.add(1, 0, 0);
        BlockPos boost5 = blockPos.add(-1, 0, 0);
        BlockPos boost6 = blockPos.add(0, 0, 1);
        BlockPos boost7 = blockPos.add(0, 2, 0);
        BlockPos boost8 = blockPos.add(0.5D, 0.5D, 0.5D);
        BlockPos boost9 = blockPos.add(0, -1, 0);

        return HoleFill.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && HoleFill.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && HoleFill.mc.world.getBlockState(boost7).getBlock() == Blocks.AIR && (HoleFill.mc.world.getBlockState(boost3).getBlock() == Blocks.OBSIDIAN || HoleFill.mc.world.getBlockState(boost3).getBlock() == Blocks.BEDROCK) && (HoleFill.mc.world.getBlockState(boost4).getBlock() == Blocks.OBSIDIAN || HoleFill.mc.world.getBlockState(boost4).getBlock() == Blocks.BEDROCK) && (HoleFill.mc.world.getBlockState(boost5).getBlock() == Blocks.OBSIDIAN || HoleFill.mc.world.getBlockState(boost5).getBlock() == Blocks.BEDROCK) && (HoleFill.mc.world.getBlockState(boost6).getBlock() == Blocks.OBSIDIAN || HoleFill.mc.world.getBlockState(boost6).getBlock() == Blocks.BEDROCK) && HoleFill.mc.world.getBlockState(boost8).getBlock() == Blocks.AIR && (HoleFill.mc.world.getBlockState(boost9).getBlock() == Blocks.OBSIDIAN || HoleFill.mc.world.getBlockState(boost9).getBlock() == Blocks.BEDROCK);
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleFill.mc.player.posX), Math.floor(HoleFill.mc.player.posY), Math.floor(HoleFill.mc.player.posZ));
    }

    private boolean isInRange(BlockPos blockPos) {
        NonNullList positions = NonNullList.create();

        positions.addAll((Collection) this.getSphere(getPlayerPos(), (float) this.range.getValDouble(), (int) this.range.getValDouble(), false, true, 0).stream().filter(this::IsHole).collect(Collectors.toList()));
        return positions.contains(blockPos);
    }

    private List findCrystalBlocks() {
        NonNullList positions = NonNullList.create();

        positions.addAll((Collection) this.getSphere(getPlayerPos(), (float) this.range.getValDouble(), (int) this.range.getValDouble(), false, true, 0).stream().filter(this::IsHole).collect(Collectors.toList()));
        return positions;
    }

    public List getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
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

    private static void setYawAndPitch(float yaw1, float pitch1) {
        HoleFill.yaw = (double) yaw1;
        HoleFill.pitch = (double) pitch1;
        HoleFill.isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (HoleFill.isSpoofingAngles) {
            HoleFill.yaw = (double) HoleFill.mc.player.rotationYaw;
            HoleFill.pitch = (double) HoleFill.mc.player.rotationPitch;
            HoleFill.isSpoofingAngles = false;
        }

    }
}
