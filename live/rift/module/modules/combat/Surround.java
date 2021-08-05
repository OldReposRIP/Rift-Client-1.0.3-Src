package live.rift.module.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import live.rift.event.events.EventMotionUpdate;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.BlockUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {

    public Setting tp = new Setting("Center", this, true);
    public Setting rotate = new Setting("Rotate", this, true);
    public Setting silentr = new Setting("RSilent", this, true);
    public Setting bpt = new Setting("BPT", this, 4.0D, 1.0D, 8.0D, true);
    public Setting delay = new Setting("Delay", this, 1.0D, 1.0D, 20.0D, true);
    List cover = new ArrayList();
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;
    private int offsetStep = 0;
    private int delayStep = 0;
    private int totalTicksRunning = 0;
    private boolean firstRun;
    private boolean isSneaking = false;
    public static boolean isSpoofingAngles = false;
    public static float yaw;
    public static float pitch;
    private static boolean togglePitch = false;
    @EventHandler
    public Listener motionEvent = new Listener(invoke<invokedynamic>(), new Predicate[0]);

    public Surround() {
        super("Surround", 0, Category.COMBAT);
        this.cover.add(new BlockPos(1, -1, 0));
        this.cover.add(new BlockPos(0, -1, 1));
        this.cover.add(new BlockPos(-1, -1, 0));
        this.cover.add(new BlockPos(0, -1, -1));
    }

    public void onEnable() {
        if (Surround.mc.player == null) {
            this.disable();
        } else {
            this.firstRun = true;
            this.playerHotbarSlot = Surround.mc.player.inventory.currentItem;
            this.lastHotbarSlot = -1;
            if (this.tp.getValBoolean()) {
                double y = (double) Surround.mc.player.getPosition().getY();
                double x = (double) Surround.mc.player.getPosition().getX();
                double z = (double) Surround.mc.player.getPosition().getZ();
                Vec3d plusPlus = new Vec3d(x + 0.5D, y, z + 0.5D);
                Vec3d plusMinus = new Vec3d(x + 0.5D, y, z - 0.5D);
                Vec3d minusMinus = new Vec3d(x - 0.5D, y, z - 0.5D);
                Vec3d minusPlus = new Vec3d(x - 0.5D, y, z + 0.5D);

                if (this.getDst(plusPlus) < this.getDst(plusMinus) && this.getDst(plusPlus) < this.getDst(minusMinus) && this.getDst(plusPlus) < this.getDst(minusPlus)) {
                    x = (double) Surround.mc.player.getPosition().getX() + 0.5D;
                    z = (double) Surround.mc.player.getPosition().getZ() + 0.5D;
                    this.centerPlayer(x, y, z);
                }

                if (this.getDst(plusMinus) < this.getDst(plusPlus) && this.getDst(plusMinus) < this.getDst(minusMinus) && this.getDst(plusMinus) < this.getDst(minusPlus)) {
                    x = (double) Surround.mc.player.getPosition().getX() + 0.5D;
                    z = (double) Surround.mc.player.getPosition().getZ() - 0.5D;
                    this.centerPlayer(x, y, z);
                }

                if (this.getDst(minusMinus) < this.getDst(plusPlus) && this.getDst(minusMinus) < this.getDst(plusMinus) && this.getDst(minusMinus) < this.getDst(minusPlus)) {
                    x = (double) Surround.mc.player.getPosition().getX() - 0.5D;
                    z = (double) Surround.mc.player.getPosition().getZ() - 0.5D;
                    this.centerPlayer(x, y, z);
                }

                if (this.getDst(minusPlus) < this.getDst(plusPlus) && this.getDst(minusPlus) < this.getDst(plusMinus) && this.getDst(minusPlus) < this.getDst(minusMinus)) {
                    x = (double) Surround.mc.player.getPosition().getX() - 0.5D;
                    z = (double) Surround.mc.player.getPosition().getZ() + 0.5D;
                    this.centerPlayer(x, y, z);
                }
            }

        }
    }

    public void onDisable() {
        if (Surround.mc.player != null) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                Surround.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
            }

            if (this.isSneaking) {
                Surround.mc.player.connection.sendPacket(new CPacketEntityAction(Surround.mc.player, Action.STOP_SNEAKING));
                this.isSneaking = false;
            }

            this.playerHotbarSlot = -1;
            this.lastHotbarSlot = -1;
            resetRotation();
        }
    }

    public void onUpdate() {
        if (Surround.mc.player != null) {
            double ogPlayerY = Surround.mc.player.posY;

            if (Surround.mc.player.posY > ogPlayerY + 1.0D) {
                this.disable();
            }

            if (this.findObiInHotbar() != -1 && this.findObiInHotbar() >= 0 && this.findObiInHotbar() <= 8) {
                if (!this.firstRun) {
                    if ((double) this.delayStep < this.delay.getValDouble()) {
                        ++this.delayStep;
                        return;
                    }

                    this.delayStep = 0;
                }

                if (this.firstRun) {
                    this.firstRun = false;
                }

                if (this.isSurrounded()) {
                    resetRotation();
                }

                int blocksPlaced;

                for (blocksPlaced = 0; (double) blocksPlaced < this.bpt.getValDouble(); ++this.offsetStep) {
                    new ArrayList();
                    boolean maxSteps = false;
                    ArrayList offsetPattern = this.getSurround();
                    int i = this.getSurround().size();

                    if (this.offsetStep >= i) {
                        this.offsetStep = 0;
                        break;
                    }

                    BlockPos offsetPos = new BlockPos((Vec3d) offsetPattern.get(this.offsetStep));
                    BlockPos targetPos = (new BlockPos(Surround.mc.player.getPositionVector())).add(offsetPos.x, offsetPos.y, offsetPos.z);

                    if (this.placeBlock(targetPos, offsetPos.y)) {
                        ++blocksPlaced;
                    }
                }

                if (blocksPlaced > 0) {
                    if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                        Surround.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
                        this.lastHotbarSlot = this.playerHotbarSlot;
                    }

                    if (this.isSneaking) {
                        Surround.mc.player.connection.sendPacket(new CPacketEntityAction(Surround.mc.player, Action.STOP_SNEAKING));
                        this.isSneaking = false;
                    }
                }

                ++this.totalTicksRunning;
            } else {
                this.disable();
            }
        }
    }

    public ArrayList getSurround() {
        ArrayList output = new ArrayList();
        Vec3d left = null;
        Vec3d right = null;
        Vec3d forward = null;
        Vec3d backward = null;
        EnumFacing horizontal = Surround.mc.player.getHorizontalFacing();

        if (horizontal == EnumFacing.NORTH) {
            forward = new Vec3d(0.0D, 0.0D, -1.0D);
            left = new Vec3d(-1.0D, 0.0D, 0.0D);
            right = new Vec3d(1.0D, 0.0D, 0.0D);
            backward = new Vec3d(0.0D, 0.0D, 1.0D);
        } else if (horizontal == EnumFacing.EAST) {
            forward = new Vec3d(1.0D, 0.0D, 0.0D);
            left = new Vec3d(0.0D, 0.0D, -1.0D);
            right = new Vec3d(0.0D, 0.0D, 1.0D);
            backward = new Vec3d(-1.0D, 0.0D, 0.0D);
        } else if (horizontal == EnumFacing.SOUTH) {
            forward = new Vec3d(0.0D, 0.0D, 1.0D);
            left = new Vec3d(1.0D, 0.0D, 0.0D);
            right = new Vec3d(-1.0D, 0.0D, 0.0D);
            backward = new Vec3d(0.0D, 0.0D, -1.0D);
        } else if (horizontal == EnumFacing.WEST) {
            forward = new Vec3d(-1.0D, 0.0D, 0.0D);
            left = new Vec3d(0.0D, 0.0D, 1.0D);
            right = new Vec3d(0.0D, 0.0D, -1.0D);
            backward = new Vec3d(1.0D, 0.0D, 0.0D);
        }

        Vec3d[] SURROUND = new Vec3d[] { forward.subtract(0.0D, 1.0D, 0.0D), forward, right.subtract(0.0D, 1.0D, 0.0D), right, backward.subtract(0.0D, 1.0D, 0.0D), backward, left.subtract(0.0D, 1.0D, 0.0D), left};
        Vec3d[] avec3d = SURROUND;
        int i = SURROUND.length;

        for (int j = 0; j < i; ++j) {
            Vec3d vec = avec3d[j];
            BlockPos offsetPos = new BlockPos(vec);
            BlockPos targetPos = (new BlockPos(Surround.mc.player.getPositionVector())).add(offsetPos.x, offsetPos.y, offsetPos.z);
            Block block = Surround.mc.world.getBlockState(targetPos).getBlock();

            if (block instanceof BlockAir || block instanceof BlockLiquid) {
                output.add(vec);
            }
        }

        return output;
    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);

        setYawAndPitch((float) v[0], (float) v[1]);
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;
        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        pitch = pitch * 180.0D / 3.141592653589793D;
        yaw = yaw * 180.0D / 3.141592653589793D;
        yaw += 90.0D;
        return new double[] { yaw, pitch};
    }

    private boolean placeBlock(BlockPos pos, int yLevel) {
        Block block = Surround.mc.world.getBlockState(pos).getBlock();

        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        } else {
            Iterator side = Surround.mc.world.getEntitiesWithinAABBExcludingEntity((Entity) null, new AxisAlignedBB(pos)).iterator();

            while (side.hasNext()) {
                Entity neighbour = (Entity) side.next();

                if (!(neighbour instanceof EntityItem) && !(neighbour instanceof EntityXPOrb)) {
                    return false;
                }
            }

            EnumFacing side1 = BlockUtil.getPlaceableSide(pos);

            if (side1 == null) {
                return false;
            } else {
                BlockPos neighbour1 = pos.offset(side1);
                EnumFacing opposite = side1.getOpposite();

                if (!BlockUtil.canBeClicked(neighbour1)) {
                    return false;
                } else {
                    Vec3d hitVec = (new Vec3d(neighbour1)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(opposite.getDirectionVec())).scale(0.5D));
                    Block neighbourBlock = Surround.mc.world.getBlockState(neighbour1).getBlock();
                    int obiSlot = this.findObiInHotbar();

                    if (obiSlot == -1) {
                        this.disable();
                    }

                    if (this.rotate.getValBoolean()) {
                        if (!this.silentr.getValBoolean()) {
                            if (yLevel != 1) {
                                BlockUtil.faceVectorPacketInstant(hitVec);
                            }
                        } else if (yLevel != 1) {
                            this.vecLookAt(hitVec);
                        }
                    }

                    if (this.lastHotbarSlot != obiSlot) {
                        Surround.mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                        this.lastHotbarSlot = obiSlot;
                    }

                    if (!this.isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
                        Surround.mc.player.connection.sendPacket(new CPacketEntityAction(Surround.mc.player, Action.START_SNEAKING));
                        this.isSneaking = true;
                    }

                    Surround.mc.playerController.processRightClickBlock(Surround.mc.player, Surround.mc.world, neighbour1, opposite, hitVec, EnumHand.MAIN_HAND);
                    Surround.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    Surround.mc.rightClickDelayTimer = 4;
                    if (Surround.isSpoofingAngles) {
                        EntityPlayerSP player2;

                        if (Surround.togglePitch) {
                            player2 = Surround.mc.player;
                            player2.rotationPitch += 4.0E-4F;
                            Surround.togglePitch = false;
                        } else {
                            player2 = Surround.mc.player;
                            player2.rotationPitch -= 4.0E-4F;
                            Surround.togglePitch = true;
                        }
                    }

                    resetRotation();
                    return true;
                }
            }
        }
    }

    private void vecLookAt(Vec3d vec) {
        float[] rotations = BlockUtil.getLegitRotations(vec);

        setYawAndPitch(rotations[0], rotations[1]);
    }

    public boolean isSurrounded() {
        int blockCount = 0;
        Iterator iterator = this.cover.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = (BlockPos) iterator.next();
            Block block = Surround.mc.world.getBlockState(Surround.mc.player.getPosition().add(pos.x, pos.y, pos.z)).getBlock();

            if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
                ++blockCount;
            }
        }

        if (blockCount == 4) {
            return true;
        } else {
            return false;
        }
    }

    private int findObiInHotbar() {
        int slot = -1;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Surround.mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }

                if (block instanceof BlockEnderChest) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    private void centerPlayer(double x, double y, double z) {
        Surround.mc.player.connection.sendPacket(new Position(x, y, z, true));
        Surround.mc.player.setPosition(x, y, z);
    }

    private double getDst(Vec3d vec) {
        return Surround.mc.player.getPositionVector().distanceTo(vec);
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        Surround.yaw = yaw1;
        Surround.pitch = pitch1;
        Surround.isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (Surround.isSpoofingAngles) {
            Surround.yaw = Surround.mc.player.rotationYaw;
            Surround.pitch = Surround.mc.player.rotationPitch;
            Surround.isSpoofingAngles = false;
        }

    }

    private static void lambda$new$0(EventMotionUpdate event) {
        if (Surround.isSpoofingAngles) {
            event.setYaw(Surround.yaw);
            event.setPitch(Surround.pitch);
            Surround.mc.player.setRotationYawHead(Surround.yaw);
        }

    }

    private static class Offsets {

        private static final Vec3d[] SURROUND = new Vec3d[] { new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, -1.0D, 0.0D), new Vec3d(0.0D, -1.0D, 1.0D), new Vec3d(-1.0D, -1.0D, 0.0D), new Vec3d(0.0D, -1.0D, -1.0D)};
    }
}
