package live.rift.module.modules.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import live.rift.event.events.RenderEvent;
import live.rift.friends.Friends;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.BlockInteractionHelper;
import live.rift.util.RiftRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;

public class AutoTrap extends Module {

    Setting range = new Setting("Range", this, 5.5D, 2.0D, 7.0D, false);
    Setting noGlitchBlocks = new Setting("NoGlitchBlocks", this, false);
    Setting blocksPerTick = new Setting("Blocks Per Tick", this, 2.0D, 1.0D, 10.0D, true);
    Setting tickDelay = new Setting("Delay", this, 2.0D, 1.0D, 20.0D, true);
    Setting cage;
    Setting messages;
    Setting timeout;
    Setting checkOnGround = new Setting("OnGround Check", this, true);
    Setting renderPlacing;
    ArrayList cages = new ArrayList();
    EntityPlayer closestTarget;
    private String lastTargetName;
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;
    private boolean isSneaking = false;
    private int delayStep = 0;
    private int offsetStep = 0;
    private boolean firstRun;
    private boolean missingObiDisable = false;
    int delay;

    public AutoTrap() {
        super("AutoTrap", 0, Category.COMBAT);
        this.cage = new Setting("Cage Type", this, "Full", this.cages);
        this.cages.add("No Scaffold Trap");
        this.cages.add("City");
        this.cages.add("Trap");
        this.messages = new Setting("Status Messages", this, true);
        this.timeout = new Setting("Disable Time Out", this, 5.0D, 2.0D, 30.0D, true);
        this.renderPlacing = new Setting("Render", this, true);
    }

    private static EnumFacing getPlaceableSide(BlockPos pos) {
        EnumFacing[] aenumfacing = EnumFacing.values();
        int i = aenumfacing.length;

        for (int j = 0; j < i; ++j) {
            EnumFacing side = aenumfacing[j];
            BlockPos neighbour = pos.offset(side);

            if (AutoTrap.mc.world.getBlockState(neighbour).getBlock().canCollideCheck(AutoTrap.mc.world.getBlockState(neighbour), false)) {
                IBlockState blockState = AutoTrap.mc.world.getBlockState(neighbour);

                if (!blockState.getMaterial().isReplaceable()) {
                    return side;
                }
            }
        }

        return null;
    }

    public void onEnable() {
        this.delay = (int) (this.timeout.getValDouble() * 40.0D);
        if (this.messages.getValBoolean()) {
            Messages.sendChatMessage("AutoTrap &2&l Enabled!");
        }

        if (AutoTrap.mc.player != null && AutoTrap.mc.player.getHealth() > 0.0F) {
            this.firstRun = true;
            this.playerHotbarSlot = AutoTrap.mc.player.inventory.currentItem;
            this.lastHotbarSlot = -1;
        }
    }

    public void onDisable() {
        if (this.messages.getValBoolean()) {
            Messages.sendChatMessage("AutoTrap &4&lDisabled!");
        }

        if (AutoTrap.mc.player != null && AutoTrap.mc.player.getHealth() > 0.0F) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                AutoTrap.mc.player.inventory.currentItem = this.playerHotbarSlot;
            }

            if (this.isSneaking) {
                AutoTrap.mc.player.connection.sendPacket(new CPacketEntityAction(AutoTrap.mc.player, Action.STOP_SNEAKING));
                this.isSneaking = false;
            }

            this.playerHotbarSlot = -1;
            this.lastHotbarSlot = -1;
            this.missingObiDisable = false;
        }
    }

    public void onUpdate() {
        if (AutoTrap.mc.player != null && AutoTrap.mc.player.getHealth() > 0.0F) {
            if (!this.checkOnGround.getValBoolean() || AutoTrap.mc.player.onGround) {
                --this.delay;
                if (this.delay <= 0) {
                    this.disable();
                }

                if (this.firstRun) {
                    if (this.findObiInHotbar() == -1) {
                        this.disable();
                        return;
                    }
                } else {
                    if ((double) this.delayStep < this.tickDelay.getValDouble()) {
                        ++this.delayStep;
                        return;
                    }

                    this.delayStep = 0;
                }

                this.findClosestTarget();
                if (this.closestTarget != null) {
                    this.modInfo = this.closestTarget.getName().toUpperCase();
                    if (this.firstRun) {
                        this.firstRun = false;
                        this.lastTargetName = this.closestTarget.getName();
                    } else if (!this.lastTargetName.equals(this.closestTarget.getName())) {
                        this.offsetStep = 0;
                        this.lastTargetName = this.closestTarget.getName();
                    }

                    ArrayList placeTargets = new ArrayList();

                    if (this.cage.getValString().equalsIgnoreCase("trap")) {
                        Collections.addAll(placeTargets, AutoTrap.Offsets.TRAPNOSCAFFOLD);
                    }

                    if (this.cage.getValString().equalsIgnoreCase("no scaffold trap")) {
                        Collections.addAll(placeTargets, AutoTrap.Offsets.TRAP);
                    }

                    if (this.cage.getValString().equalsIgnoreCase("city")) {
                        Collections.addAll(placeTargets, AutoTrap.Offsets.CRYSTALEXA);
                    }

                    int blocksPlaced;

                    for (blocksPlaced = 0; (double) blocksPlaced < this.blocksPerTick.getValDouble(); ++this.offsetStep) {
                        if (this.offsetStep >= placeTargets.size()) {
                            this.offsetStep = 0;
                            break;
                        }

                        BlockPos offsetPos = new BlockPos((Vec3d) placeTargets.get(this.offsetStep));
                        BlockPos targetPos = (new BlockPos(this.closestTarget.getPositionVector())).down().add(offsetPos.x, offsetPos.y, offsetPos.z);

                        if (this.placeBlockInRange(targetPos, this.range.getValDouble())) {
                            ++blocksPlaced;
                        }
                    }

                    if (blocksPlaced > 0) {
                        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                            AutoTrap.mc.player.inventory.currentItem = this.playerHotbarSlot;
                            this.lastHotbarSlot = this.playerHotbarSlot;
                        }

                        if (this.isSneaking) {
                            AutoTrap.mc.player.connection.sendPacket(new CPacketEntityAction(AutoTrap.mc.player, Action.STOP_SNEAKING));
                            this.isSneaking = false;
                        }
                    }

                    if (this.missingObiDisable) {
                        this.missingObiDisable = false;
                        this.disable();
                    }

                }
            }
        }
    }

    public void onWorld(RenderEvent e) {
        if (this.renderPlacing.getValBoolean()) {
            ArrayList placeTargets = new ArrayList();

            if (this.cage.getValString().equalsIgnoreCase("trap")) {
                Collections.addAll(placeTargets, AutoTrap.Offsets.TRAP);
            }

            if (this.cage.getValString().equalsIgnoreCase("no scaffold trap")) {
                Collections.addAll(placeTargets, AutoTrap.Offsets.TRAP);
            }

            if (this.cage.getValString().equalsIgnoreCase("city")) {
                Collections.addAll(placeTargets, AutoTrap.Offsets.CRYSTALEXA);
            }

            Iterator iterator = placeTargets.iterator();

            while (iterator.hasNext()) {
                Vec3d p = (Vec3d) iterator.next();

                if (this.closestTarget != null) {
                    BlockPos a = new BlockPos(this.closestTarget.getPositionVector().add(0.0D, -1.0D, 0.0D).add(p.x, p.y, p.z));

                    if (AutoTrap.mc.world.getBlockState(a).getBlock() == Blocks.AIR) {
                        RiftRenderer.prepare(7);
                        RiftRenderer.drawBoundingBoxBlockPos(a, 1.0F, 255, 255, 255, 75);
                        RiftRenderer.release();
                    }
                }
            }
        }

    }

    private boolean placeBlockInRange(BlockPos pos, double range) {
        Block block = AutoTrap.mc.world.getBlockState(pos).getBlock();

        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        } else {
            Iterator side = AutoTrap.mc.world.getEntitiesWithinAABBExcludingEntity((Entity) null, new AxisAlignedBB(pos)).iterator();

            while (side.hasNext()) {
                Entity neighbour = (Entity) side.next();

                if (!(neighbour instanceof EntityItem) && !(neighbour instanceof EntityXPOrb)) {
                    return false;
                }
            }

            EnumFacing side1 = getPlaceableSide(pos);

            if (side1 == null) {
                return false;
            } else {
                BlockPos neighbour1 = pos.offset(side1);
                EnumFacing opposite = side1.getOpposite();

                if (!BlockInteractionHelper.canBeClicked(neighbour1)) {
                    return false;
                } else {
                    Vec3d hitVec = (new Vec3d(neighbour1)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(opposite.getDirectionVec())).scale(0.5D));
                    Block neighbourBlock = AutoTrap.mc.world.getBlockState(neighbour1).getBlock();

                    if (AutoTrap.mc.player.getPositionVector().distanceTo(hitVec) > range) {
                        return false;
                    } else {
                        int obiSlot = this.findObiInHotbar();

                        if (obiSlot == -1) {
                            this.missingObiDisable = true;
                            return false;
                        } else {
                            if (this.lastHotbarSlot != obiSlot) {
                                AutoTrap.mc.player.inventory.currentItem = obiSlot;
                                this.lastHotbarSlot = obiSlot;
                            }

                            if (!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
                                AutoTrap.mc.player.connection.sendPacket(new CPacketEntityAction(AutoTrap.mc.player, Action.START_SNEAKING));
                                this.isSneaking = true;
                            }

                            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                            AutoTrap.mc.playerController.processRightClickBlock(AutoTrap.mc.player, AutoTrap.mc.world, neighbour1, opposite, hitVec, EnumHand.MAIN_HAND);
                            AutoTrap.mc.player.swingArm(EnumHand.MAIN_HAND);
                            AutoTrap.mc.rightClickDelayTimer = 4;
                            if (this.noGlitchBlocks.getValBoolean() && !AutoTrap.mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                                AutoTrap.mc.playerController.processRightClickBlock(AutoTrap.mc.player, AutoTrap.mc.world, pos, EnumFacing.NORTH, (new Vec3d((double) pos.x + 0.5D, (double) pos.y + 0.5D, (double) pos.z + 0.5D)).add((new Vec3d(EnumFacing.NORTH.getDirectionVec())).scale(0.5D)), EnumHand.OFF_HAND);
                            }

                            return true;
                        }
                    }
                }
            }
        }
    }

    private int findObiInHotbar() {
        int slot = -1;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoTrap.mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    private void findClosestTarget() {
        List playerList = AutoTrap.mc.world.playerEntities;

        this.closestTarget = null;
        Iterator iterator = playerList.iterator();

        while (iterator.hasNext()) {
            EntityPlayer target = (EntityPlayer) iterator.next();

            if (target != AutoTrap.mc.player && (double) AutoTrap.mc.player.getDistance(target) <= this.range.getValDouble() + 3.0D && target.getHealth() > 0.0F && !Friends.isFriend(target.getName())) {
                if (this.closestTarget == null) {
                    this.closestTarget = target;
                } else if (AutoTrap.mc.player.getDistance(target) < AutoTrap.mc.player.getDistance(this.closestTarget)) {
                    this.closestTarget = target;
                }
            }
        }

    }

    private static class Offsets {

        private static final Vec3d[] TRAPNOSCAFFOLD = new Vec3d[] { new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 2.0D, -1.0D), new Vec3d(1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 2.0D, 1.0D), new Vec3d(-1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 0.0D), new Vec3d(0.0D, 4.0D, 0.0D)};
        private static final Vec3d[] CRYSTALEXA = new Vec3d[] { new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(0.0D, 2.0D, -1.0D), new Vec3d(0.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 0.0D), new Vec3d(-1.0D, 3.0D, 0.0D), new Vec3d(-1.0D, 2.0D, 0.0D), new Vec3d(1.0D, 3.0D, 0.0D), new Vec3d(1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 3.0D, 1.0D), new Vec3d(0.0D, 2.0D, 1.0D)};
        private static final Vec3d[] TRAP = new Vec3d[] { new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 2.0D, -1.0D), new Vec3d(1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 2.0D, 1.0D), new Vec3d(-1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 0.0D)};
    }
}
