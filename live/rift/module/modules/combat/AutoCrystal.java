package live.rift.module.modules.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import live.rift.RiftMod;
import live.rift.event.events.PacketEvent;
import live.rift.event.events.RenderEvent;
import live.rift.friends.Friends;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.ModuleManager;
import live.rift.setting.Setting;
import live.rift.util.RainbowUtil;
import live.rift.util.RiftRenderer;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

public class AutoCrystal extends Module {

    private final Setting range = new Setting("Range", this, 5.5D, 1.0D, 10.0D, false);
    private final Setting wallRange = new Setting("Wall Range", this, 3.0D, 0.0D, 10.0D, false);
    private final Setting enemyRange = new Setting("Enemy Range", this, 13.0D, 8.0D, 18.0D, true);
    private Setting hitDelay = new Setting("Hit Delay", this, 3.0D, 1.0D, 20.0D, true);
    private final Setting placeDelay = new Setting("Calc Delay", this, 3.0D, 1.0D, 20.0D, true);
    private final Setting useDelay = new Setting("Place Delay", this, 0.5D, 0.5D, 20.0D, false);
    private final Setting own;
    private final Setting hitMode;
    private final Setting minDamage = new Setting("Minimum Damage", this, 8.0D, 1.0D, 16.0D, true);
    private final Setting maxSelfDmg = new Setting("Max Self Damage", this, 5.0D, 1.0D, 16.0D, true);
    private final Setting facePlace = new Setting("Faceplace", this, true);
    private final Setting facePlaceHealth = new Setting("Faceplace Health", this, 10.0D, 1.0D, 36.0D, true);
    private final Setting enableMessages;
    private final Setting pSilent = new Setting("PSilent", this, false);
    private final Setting autoSwitch;
    private final Setting noGapSwitch;
    private final Setting noToolSwitch;
    private final Setting noXPSwitch;
    private final Setting targetRender;
    private final Setting onGroundExplode = new Setting("Stop In Air", this, false);
    private Setting sendOnGroundPackets = new Setting("Spoof OnGround", this, false);
    private final Setting strict = new Setting("NCP Strict", this, true);
    private final Setting koh = new Setting("Kill On Hit", this, false);
    private final Setting multi;
    private final Setting targetDamageCheck = new Setting("Check Explode Damage", this, false);
    private final ArrayList hitModes = new ArrayList();
    double damage;
    boolean p = false;
    boolean h = false;
    public EntityEnderCrystal placed;
    public Entity target;
    int useTicks;
    int placeTicks;
    int hitTicks;
    int switchTicks = 0;
    boolean resetTick;
    EntityEnderCrystal crystal = null;
    EntityEnderCrystal lastCrystal = null;
    BlockPos lastPos;
    double lastDmg;
    double lastSelf;
    BlockPos q;
    boolean offhand;
    String damageText = "0.0";
    int sinceTC = 0;
    public ArrayList crystalsPP = new ArrayList();
    int ri = 0;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    @EventHandler
    private Listener cPacketListener = new Listener((event) -> {
        Packet packet = event.getPacket();

        if (packet instanceof CPacketPlayer) {
            if (this.hitTicks == 0 && this.sendOnGroundPackets.getValBoolean()) {
                ((CPacketPlayer) packet).onGround = true;
            } else if ((double) this.hitTicks >= this.hitDelay.getValDouble() * 2.0D && this.sendOnGroundPackets.getValBoolean() && !((CPacketPlayer) packet).onGround) {
                ((CPacketPlayer) packet).onGround = false;
            }

            if (AutoCrystal.isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) AutoCrystal.yaw;
                ((CPacketPlayer) packet).pitch = (float) AutoCrystal.pitch;
            }
        }

    }, new Predicate[0]);
    @EventHandler
    private Listener sPacketListener = new Listener((event) -> {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect p = (SPacketSoundEffect) event.getPacket();

            if (p.getCategory() == SoundCategory.BLOCKS && p.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                Iterator iterator = (new ArrayList(AutoCrystal.mc.world.loadedEntityList)).iterator();

                while (iterator.hasNext()) {
                    Entity e = (Entity) iterator.next();

                    if (e instanceof EntityEnderCrystal && e.getDistance(p.getX(), p.getY(), p.getZ()) <= 6.0D) {
                        e.setDead();
                    }
                }
            }
        }

        if (event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject p1 = (SPacketSpawnObject) event.getPacket();

            if (p1.getType() == 51 && this.q != null && (new BlockPos(p1.getX(), p1.getY(), p1.getZ())).down() == this.q) {
                this.crystalsPP.add(p1.getUniqueId());
            }
        }

    }, new Predicate[0]);
    RainbowUtil rutil = new RainbowUtil(9);

    public AutoCrystal() {
        super("AutoCrystal", 0, Category.COMBAT);
        this.hitMode = new Setting("Hit Priority", this, "Distance", this.hitModes);
        this.hitModes.add("Damage");
        this.hitModes.add("Distance");
        this.own = new Setting("OnlyOwn", this, true);
        this.multi = new Setting("MultiPlace", this, true);
        this.autoSwitch = new Setting("AutoSwitch", this, true);
        this.noGapSwitch = new Setting("NoGapSwitch", this, true);
        this.noToolSwitch = new Setting("NoToolSwitch", this, false);
        this.noXPSwitch = new Setting("NoXPSwitch", this, false);
        this.enableMessages = new Setting("Status Messages", this, true);
        this.targetRender = new Setting("Outline Target", this, true);
    }

    public void onEnable() {
        this.p = false;
        this.h = false;
        if (this.enableMessages.getValBoolean()) {
            Messages.sendChatMessage("CrystalAura is &2&lSTRONG");
        }

    }

    public void onDisable() {
        if (this.enableMessages.getValBoolean()) {
            Messages.sendChatMessage("CrystalAura is &4&lWEAK");
        }

    }

    public void onUpdate() {
        if (AutoCrystal.mc.world != null) {
            if (this.resetTick) {
                this.resetTick = false;
                resetRotation();
            }

            ++this.useTicks;
            ++this.hitTicks;
            ++this.placeTicks;
            this.target = (Entity) AutoCrystal.mc.world.loadedEntityList.stream().filter((e) -> {
                return e instanceof EntityPlayer && !e.equals(AutoCrystal.mc.player) && (double) e.getDistance(AutoCrystal.mc.player) <= this.enemyRange.getValDouble();
            }).filter((e) -> {
                return !Friends.isFriend(e.getName()) && ((double) (((EntityLivingBase) e).getHealth() + ((EntityLivingBase) e).getAbsorptionAmount()) < this.facePlaceHealth.getValDouble() && this.isInHole(e) || !this.isInHole(e));
            }).map((e) -> {
                return (EntityPlayer) e;
            }).min(Comparator.comparing((e) -> {
                return Float.valueOf(AutoCrystal.mc.player.getDistance(e));
            })).orElse((Object) null);
            this.modInfo = this.target == null ? "NO TARGET" : this.target.getName().toUpperCase();
            EntityEnderCrystal crystal = null;

            if (this.placed == null) {
                this.placed = new EntityEnderCrystal(AutoCrystal.mc.world, 0.0D, 0.0D, 0.0D);
                this.placed.setDead();
            }

            double health = (double) (AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount());

            if (this.hitMode.getValString().equalsIgnoreCase("distance")) {
                crystal = (EntityEnderCrystal) AutoCrystal.mc.world.loadedEntityList.stream().filter((entity) -> {
                    return (double) entity.getDistance(AutoCrystal.mc.player) <= this.range.getValDouble();
                }).filter((entity) -> {
                    return entity instanceof EntityEnderCrystal;
                }).filter((entity) -> {
                    return health <= this.maxSelfDmg.getValDouble() ? (double) calculateDamage((EntityEnderCrystal) entity, AutoCrystal.mc.player) < health : (double) calculateDamage((EntityEnderCrystal) entity, AutoCrystal.mc.player) <= this.maxSelfDmg.getValDouble();
                }).filter((entity) -> {
                    return this.own.getValBoolean() ? this.crystalsPP.contains(entity.getUniqueID()) : true;
                }).filter((entity) -> {
                    return this.targetDamageCheck.getValBoolean() ? (this.target != null ? (double) calculateDamage((EntityEnderCrystal) entity, this.target) >= ((double) (((EntityLivingBase) this.target).getHealth() + ((EntityLivingBase) this.target).getAbsorptionAmount()) < this.facePlaceHealth.getValDouble() ? 2.0D : this.minDamage.getValDouble()) : true) : true;
                }).map((entity) -> {
                    return (EntityEnderCrystal) entity;
                }).min(Comparator.comparing((c) -> {
                    return Float.valueOf(AutoCrystal.mc.player.getDistance(c));
                })).orElse((Object) null);
                this.lastCrystal = crystal;
            } else if (this.hitMode.getValString().equalsIgnoreCase("damage")) {
                crystal = (EntityEnderCrystal) AutoCrystal.mc.world.loadedEntityList.stream().filter((entity) -> {
                    return (double) entity.getDistance(AutoCrystal.mc.player) <= this.range.getValDouble();
                }).filter((entity) -> {
                    return entity instanceof EntityEnderCrystal;
                }).filter((entity) -> {
                    return health <= this.maxSelfDmg.getValDouble() ? (double) calculateDamage((EntityEnderCrystal) entity, AutoCrystal.mc.player) < health : (double) calculateDamage((EntityEnderCrystal) entity, AutoCrystal.mc.player) <= this.maxSelfDmg.getValDouble();
                }).filter((entity) -> {
                    return this.own.getValBoolean() ? this.crystalsPP.contains(entity.getUniqueID()) : true;
                }).filter((entity) -> {
                    return this.targetDamageCheck.getValBoolean() ? (this.target != null ? (double) calculateDamage((EntityEnderCrystal) entity, this.target) >= ((double) (((EntityLivingBase) this.target).getHealth() + ((EntityLivingBase) this.target).getAbsorptionAmount()) < this.facePlaceHealth.getValDouble() ? 2.0D : this.minDamage.getValDouble()) : true) : true;
                }).map((entity) -> {
                    return (EntityEnderCrystal) entity;
                }).max(Comparator.comparing((c) -> {
                    return Float.valueOf(calculateDamage(c, this.target));
                })).orElse((Object) null);
                this.lastCrystal = crystal;
            }

            try {
                label315: {
                    if (crystal == null) {
                        throw new Exception();
                    }

                    if (this.noXPSwitch.getValBoolean() && AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE && AutoCrystal.mc.gameSettings.keyBindUseItem.isKeyDown() || this.noGapSwitch.getValBoolean() && AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE && AutoCrystal.mc.gameSettings.keyBindUseItem.isKeyDown() && this.strict.getValBoolean()) {
                        throw new Exception();
                    }

                    if (AutoCrystal.mc.player.canEntityBeSeen(crystal)) {
                        if ((double) AutoCrystal.mc.player.getDistance(crystal) > this.range.getValDouble()) {
                            break label315;
                        }
                    } else if ((double) AutoCrystal.mc.player.getDistance(crystal) > this.wallRange.getValDouble()) {
                        break label315;
                    }

                    if ((double) this.hitTicks >= this.hitDelay.getValDouble() * 2.0D && (AutoCrystal.mc.player.onGround && this.onGroundExplode.getValBoolean() || !this.onGroundExplode.getValBoolean())) {
                        this.hitTicks = 0;
                        this.explode(crystal);
                        if (AutoCrystal.mc.player.canEntityBeSeen(crystal) && this.koh.getValBoolean()) {
                            crystal.setDead();
                        }

                        if (crystal.isDead) {
                            this.p = false;
                            this.h = true;
                        }
                    }
                }
            } catch (Exception exception) {
                ;
            }

            if (this.pSilent.getValBoolean()) {
                resetRotation();
            } else {
                this.resetTick = true;
            }

            if (!this.p || this.placed == null || this.placed.isDead || this.multi.getValBoolean()) {
                if (this.target != null && (double) this.placeTicks >= this.placeDelay.getValDouble() * 2.0D) {
                    this.crystalsPP.clear();
                    this.placeTicks = 0;
                    this.q = null;
                    double result = (double) (((EntityLivingBase) this.target).getHealth() + ((EntityLivingBase) this.target).getAbsorptionAmount());
                    double l = (double) (AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount());
                    double s = 0.5D;
                    double d = 0.5D;

                    this.damage = 0.5D;
                    Iterator iterator = this.findCrystalBlocks().iterator();

                    while (iterator.hasNext()) {
                        BlockPos b = (BlockPos) iterator.next();

                        s = (double) calculateDamage((double) b.x + 0.5D, (double) (b.y + 1), (double) b.z + 0.5D, AutoCrystal.mc.player);
                        d = (double) calculateDamage((double) b.x + 0.5D, (double) (b.y + 1), (double) b.z + 0.5D, this.target);
                        if (!this.target.isDead && result > 0.0D && s <= d && s <= this.maxSelfDmg.getValDouble() && s <= l - 1.0D && this.target instanceof EntityPlayer && !Friends.isFriend(((EntityPlayer) this.target).getName())) {
                            if (this.facePlace.getValBoolean() && result <= this.facePlaceHealth.getValDouble() && this.isInHole(this.target) && b.getDistance((int) this.target.getPositionVector().x, (int) this.target.getPositionVector().y, (int) this.target.getPositionVector().z) <= 1.0D && d > 2.0D) {
                                this.q = b;
                                this.damage = d;
                                break;
                            }

                            if (d > this.minDamage.getValDouble()) {
                                this.q = b;
                                this.damage = d;
                                break;
                            }
                        }
                    }
                }

                if (this.q != null) {
                    if (this.target != null) {
                        if (this.lastPos == null) {
                            this.lastPos = this.q;
                        }

                        this.lastDmg = (double) calculateDamage((double) this.lastPos.x + 0.5D, (double) (this.lastPos.y + 1), (double) this.lastPos.z + 0.5D, this.target);
                        this.lastSelf = (double) calculateDamage((double) this.lastPos.x + 0.5D, (double) (this.lastPos.y + 1), (double) this.lastPos.z + 0.5D, AutoCrystal.mc.player);
                        if (this.lastDmg >= this.damage && this.lastSelf < this.maxSelfDmg.getValDouble() && this.lastDmg >= this.minDamage.getValDouble() && this.lastDmg >= this.lastSelf && !this.multi.getValBoolean()) {
                            this.q = this.lastPos;
                            this.damage = this.lastDmg;
                        }

                        this.lastPos = this.q;
                        ++this.switchTicks;
                        if (this.autoSwitch.getValBoolean()) {
                            try {
                                boolean flag = false;

                                if (this.switchTicks <= 2 && this.strict.getValBoolean()) {
                                    throw new Exception();
                                }

                                if (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
                                    flag = true;
                                }

                                if (this.noGapSwitch.getValBoolean() && AutoCrystal.mc.player.getHeldItemMainhand().getItem() instanceof ItemFood && !flag && AutoCrystal.mc.gameSettings.keyBindUseItem.isKeyDown()) {
                                    throw new Exception();
                                }

                                if (this.noToolSwitch.getValBoolean() && AutoCrystal.mc.player.getHeldItemMainhand().getItem() instanceof ItemTool && AutoCrystal.mc.gameSettings.keyBindAttack.isKeyDown()) {
                                    throw new Exception();
                                }

                                if (this.noXPSwitch.getValBoolean() && AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE && AutoCrystal.mc.gameSettings.keyBindUseItem.isKeyDown()) {
                                    throw new Exception();
                                }

                                int f = AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? AutoCrystal.mc.player.inventory.currentItem : -1;

                                if (f == -1) {
                                    for (int i = 0; i < 9; ++i) {
                                        if (AutoCrystal.mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                                            f = i;
                                            break;
                                        }
                                    }
                                }

                                flag = false;
                                if (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
                                    flag = true;
                                } else {
                                    if (f == -1) {
                                        return;
                                    }

                                    AutoCrystal.mc.player.inventory.currentItem = f;
                                }

                                this.switchTicks = 0;
                            } catch (Exception exception1) {
                                ;
                            }
                        }

                        this.lookAtPacket((double) this.q.x + 0.5D, (double) this.q.y - 0.5D, (double) this.q.z + 0.5D, AutoCrystal.mc.player);
                        RayTraceResult raytraceresult = AutoCrystal.mc.world.rayTraceBlocks(new Vec3d(AutoCrystal.mc.player.posX, AutoCrystal.mc.player.posY + (double) AutoCrystal.mc.player.getEyeHeight(), AutoCrystal.mc.player.posZ), new Vec3d((double) this.q.x + 0.5D, (double) this.q.y - 0.5D, (double) this.q.z + 0.5D));
                        EnumFacing enumfacing;

                        if (raytraceresult != null && raytraceresult.sideHit != null) {
                            enumfacing = raytraceresult.sideHit;
                        } else {
                            enumfacing = EnumFacing.UP;
                        }

                        if (this.canPlaceCrystal(this.q) && (double) this.useTicks >= this.useDelay.getValDouble() * 2.0D) {
                            this.useTicks = 0;
                            AutoCrystal.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.q, enumfacing, AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                            this.placed = (EntityEnderCrystal) AutoCrystal.mc.world.loadedEntityList.stream().filter((e) -> {
                                return e instanceof EntityEnderCrystal;
                            }).filter((e) -> {
                                return e.getPosition() == this.q.add(0, 1, 0);
                            }).map((e) -> {
                                return (EntityEnderCrystal) e;
                            }).min(Comparator.comparing((e) -> {
                                return Float.valueOf(AutoCrystal.mc.player.getDistance(e));
                            })).orElse((Object) null);
                            if (this.pSilent.getValBoolean()) {
                                resetRotation();
                            } else {
                                this.resetTick = true;
                            }

                        }
                    }
                }
            }
        }
    }

    public void onRender() {
        this.rutil.onRender();
        if (this.ri >= 355) {
            this.ri = 0;
        }

    }

    public void onWorld(RenderEvent e) {
        int rgb = this.getColor(this.ri);
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;

        if (this.target != null) {
            if (this.targetRender.getValBoolean()) {
                RiftRenderer.prepare(7);
                Vec3d damageText = ModuleManager.getInterpolatedPos(this.target, AutoCrystal.mc.getRenderPartialTicks());
                boolean isThirdPersonFrontal = AutoCrystal.mc.getRenderManager().options.thirdPersonView == 2;
                float viewerYaw = AutoCrystal.mc.getRenderManager().playerViewY;

                GlStateManager.pushMatrix();
                GlStateManager.translate(damageText.x - AutoCrystal.mc.getRenderManager().renderPosX, damageText.y - AutoCrystal.mc.getRenderManager().renderPosY, damageText.z - AutoCrystal.mc.getRenderManager().renderPosZ);
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1), 1.0F, 0.0F, 0.0F);
                RiftRenderer.drawCSGOOutline(this.target, this.getColor(this.ri));
                RiftRenderer.release();
                GlStateManager.popMatrix();
            }

            if (this.q != null) {
                RiftRenderer.prepare(7);
                RiftRenderer.drawOTwoBoundingBoxBlockPos(this.q.up(), 1.0F, r, g, b, 255);
                RiftRenderer.release();
                RiftRenderer.prepare(7);
                RiftRenderer.drawOTwoBox(this.q.up(), r, g, b, 100, 63);
                RiftRenderer.release();
                GlStateManager.pushMatrix();
                glBillboardDistanceScaled((float) this.q.getX() + 0.5F, (float) this.q.getY() + 1.1F, (float) this.q.getZ() + 0.5F, AutoCrystal.mc.player, 1.0F);
                String damageText1 = (Math.floor((double) calculateDamage((double) this.q.x + 0.5D, (double) (this.q.y + 1), (double) this.q.z + 0.5D, this.target)) == (double) calculateDamage((double) this.q.x + 0.5D, (double) (this.q.y + 1), (double) (this.q.z + 0), this.target) ? Integer.valueOf((int) calculateDamage((double) this.q.x + 0.5D, (double) this.q.y + 1.5D, (double) this.q.z + 0.5D, this.target)) : String.format("%.1f", new Object[] { Float.valueOf(calculateDamage((double) this.q.x + 0.5D, (double) this.q.y + 1.5D, (double) this.q.z + 0.5D, this.target))})) + "";

                GlStateManager.disableDepth();
                GlStateManager.translate(-((double) AutoCrystal.mc.fontRenderer.getStringWidth(damageText1) / 2.0D), 0.0D, 0.0D);
                AutoCrystal.mc.fontRenderer.drawStringWithShadow(damageText1, 0.0F, 0.0F, -197380);
                GlStateManager.popMatrix();
            }
        }
    }

    public boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);

        return (AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || AutoCrystal.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && AutoCrystal.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && AutoCrystal.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && AutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && AutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(AutoCrystal.mc.player.posX), Math.floor(AutoCrystal.mc.player.posY), Math.floor(AutoCrystal.mc.player.posZ));
    }

    private List findCrystalBlocks() {
        NonNullList positions = NonNullList.create();

        positions.addAll((Collection) getSphere(getPlayerPos(), (float) this.range.getValDouble(), (int) this.range.getValDouble(), false, true, 0).stream().filter(this::canPlaceCrystal).sorted(Comparator.comparingDouble((b) -> {
            return ((Entity) (this.target == null ? AutoCrystal.mc.player : this.target)).getDistanceSq(b);
        })).collect(Collectors.toList()));
        return positions;
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

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = (double) entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finalD = 1.0D;

        if (entity instanceof EntityLivingBase) {
            finalD = (double) getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(AutoCrystal.mc.world, (Entity) null, posX, posY, posZ, 6.0F, false, true));
        }

        return (float) finalD;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);

            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp((float) k, 0.0F, 20.0F);

            damage *= 1.0F - f / 25.0F;
            if (entity.isPotionActive((Potion) Objects.requireNonNull(Potion.getPotionById(11)))) {
                damage -= damage / 5.0F;
            }

            damage = Math.max(damage, 0.0F);
            return damage;
        } else {
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            return damage;
        }
    }

    private static float getDamageMultiplied(float damage) {
        int diff = AutoCrystal.mc.world.getDifficulty().getId();

        return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
    }

    public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
        return crystal != null && entity != null ? calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity) : 0.0F;
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        AutoCrystal.yaw = (double) yaw1;
        AutoCrystal.pitch = (double) pitch1;
        AutoCrystal.isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (AutoCrystal.isSpoofingAngles) {
            AutoCrystal.yaw = (double) AutoCrystal.mc.player.rotationYaw;
            AutoCrystal.pitch = (double) AutoCrystal.mc.player.rotationPitch;
            AutoCrystal.isSpoofingAngles = false;
        }

    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);

        AutoCrystal.mc.player.setRotationYawHead((float) v[0]);
        setYawAndPitch((float) v[0], (float) v[1] + 1.0F);
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

    public boolean isInHole(Entity e) {
        int holeBlocks = 0;
        int lowerBlocks = 0;
        BlockPos pos = new BlockPos(Math.floor(e.getPositionVector().x), Math.floor(e.getPositionVector().y), Math.floor(e.getPositionVector().z));
        BlockPos[] offset = new BlockPos[] { new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
        BlockPos[] lower = new BlockPos[] { new BlockPos(1, -1, 0), new BlockPos(-1, -1, 0), new BlockPos(0, -1, 1), new BlockPos(0, -1, -1)};
        int i = -1;
        BlockPos[] ablockpos = offset;
        int i = offset.length;

        int j;
        BlockPos l;
        BlockPos finalp;
        Block b;

        for (j = 0; j < i; ++j) {
            l = ablockpos[j];
            ++i;
            finalp = pos.add(l);
            b = AutoCrystal.mc.world.getBlockState(finalp.down()).getBlock();
            Block b1 = AutoCrystal.mc.world.getBlockState(finalp).getBlock();

            if (b1 == Blocks.OBSIDIAN || b1 == Blocks.BEDROCK) {
                ++holeBlocks;
                if (b == Blocks.AIR) {
                    --lowerBlocks;
                }
            }
        }

        ablockpos = lower;
        i = lower.length;

        for (j = 0; j < i; ++j) {
            l = ablockpos[j];
            finalp = pos.add(l);
            b = AutoCrystal.mc.world.getBlockState(finalp).getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK) {
                ++lowerBlocks;
            }
        }

        if (holeBlocks < lowerBlocks) {
            return false;
        } else {
            return holeBlocks >= 4 || holeBlocks + lowerBlocks == 4;
        }
    }

    public static void glBillboard(float x, float y, float z) {
        float scale = 0.02666667F;

        GlStateManager.translate((double) x - AutoCrystal.mc.getRenderManager().renderPosX, (double) y - AutoCrystal.mc.getRenderManager().renderPosY, (double) z - AutoCrystal.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-AutoCrystal.mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(AutoCrystal.mc.player.rotationPitch, AutoCrystal.mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
        glBillboard(x, y, z);
        int distance = (int) player.getDistance((double) x, (double) y, (double) z);
        float scaleDistance = (float) distance / 2.0F / (2.0F + (2.0F - scale));

        if (scaleDistance < 1.0F) {
            scaleDistance = 1.0F;
        }

        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }

    public void explode(EntityEnderCrystal crystal) {
        this.lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, AutoCrystal.mc.player);
        AutoCrystal.mc.playerController.attackEntity(AutoCrystal.mc.player, crystal);
        AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
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
