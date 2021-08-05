package live.rift.module.modules.movement;

import java.util.ArrayList;
import java.util.function.Predicate;
import live.rift.RiftMod;
import live.rift.event.events.EventPlayerJump;
import live.rift.event.events.PlayerMoveEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.modules.misc.TimerMod;
import live.rift.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.MathHelper;

public class Speed extends Module {

    public Setting mode;
    ArrayList modes = new ArrayList();
    public Setting autosprint;
    public Setting autojump;
    public Setting waterspeed;
    public Setting strict;
    public Setting usetimer;
    public Setting speed;
    private TimerMod Timer = null;
    @EventHandler
    private Listener OnPlayerJump = new Listener((e) -> {
        if (this.mode.getValString().equalsIgnoreCase("Strafe")) {
            e.cancel();
        }

    }, new Predicate[0]);
    @EventHandler
    private Listener OnPlayerMove = new Listener((p_Event) -> {
        if (!this.mode.getValString().equalsIgnoreCase("OnGround")) {
            if (!Speed.mc.player.isInWater() && !Speed.mc.player.isInLava() || this.waterspeed.getValBoolean()) {
                if (Speed.mc.player.capabilities == null || !Speed.mc.player.capabilities.isFlying && !Speed.mc.player.isElytraFlying()) {
                    if (!Speed.mc.player.onGround) {
                        float playerSpeed = 0.2871782F;
                        float moveForward = Speed.mc.player.movementInput.moveForward;
                        float moveStrafe = Speed.mc.player.movementInput.moveStrafe;
                        float rotationYaw = Speed.mc.player.rotationYaw;

                        if (Speed.mc.player.isPotionActive(MobEffects.SPEED)) {
                            int value = Speed.mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();

                            playerSpeed *= 1.0F + 0.2F * (float) (value + 1);
                        }

                        if (!this.strict.getValBoolean()) {
                            float value1 = (float) (1.0D + this.speed.getValDouble() * 5.0E-5D);

                            playerSpeed *= value1;
                        }

                        if (moveForward == 0.0F && moveStrafe == 0.0F) {
                            p_Event.x = 0.0D;
                            p_Event.z = 0.0D;
                        } else {
                            if (moveForward != 0.0F) {
                                if (moveStrafe > 0.0F) {
                                    rotationYaw += (float) (moveForward > 0.0F ? -45 : 45);
                                } else if (moveStrafe < 0.0F) {
                                    rotationYaw += (float) (moveForward > 0.0F ? 45 : -45);
                                }

                                moveStrafe = 0.0F;
                                if (moveForward > 0.0F) {
                                    moveForward = 1.0F;
                                } else if (moveForward < 0.0F) {
                                    moveForward = -1.0F;
                                }
                            }

                            p_Event.x = (double) (moveForward * playerSpeed) * Math.cos(Math.toRadians((double) (rotationYaw + 90.0F))) + (double) (moveStrafe * playerSpeed) * Math.sin(Math.toRadians((double) (rotationYaw + 90.0F)));
                            p_Event.z = (double) (moveForward * playerSpeed) * Math.sin(Math.toRadians((double) (rotationYaw + 90.0F))) - (double) (moveStrafe * playerSpeed) * Math.cos(Math.toRadians((double) (rotationYaw + 90.0F)));
                        }

                        p_Event.cancel();
                    }
                }
            }
        }
    }, new Predicate[0]);
    int tick = 0;

    public Speed() {
        super("Speed", 0, Category.MOVEMENT);
        this.modes.add("Strafe");
        this.modes.add("OnGround");
        this.mode = new Setting("Mode", this, "Strafe", this.modes);
        this.autosprint = new Setting("AutoSprint", this, false);
        this.autojump = new Setting("AutoJump", this, true);
        this.waterspeed = new Setting("SpeedInWater", this, true);
        this.strict = new Setting("Strict", this, false);
        this.usetimer = new Setting("UseTimer", this, true);
        this.speed = new Setting("Speed", this, 64.0D, 10.0D, 125.0D, true);
    }

    public void onUpdate() {
        if (!Speed.mc.player.isRiding()) {
            this.setModInfo(this.mode.getValString());
            if (!Speed.mc.player.isInWater() && !Speed.mc.player.isInLava() || this.waterspeed.getValBoolean()) {
                if (this.usetimer.getValBoolean() && this.Timer != null) {
                    this.Timer.SetOverrideSpeed(1.088F);
                }

                if (Speed.mc.player.moveForward != 0.0F || Speed.mc.player.moveStrafing != 0.0F) {
                    if (this.autosprint.getValBoolean()) {
                        Speed.mc.player.setSprinting(true);
                    }

                    float yaw;

                    if (Speed.mc.player.onGround && this.mode.getValString().equalsIgnoreCase("Strafe")) {
                        ++this.tick;
                        if (this.autojump.getValBoolean() && this.tick >= 2) {
                            Speed.mc.player.motionY = 0.4000000059604645D;
                            this.tick = 0;
                        }

                        yaw = this.GetRotationYawForCalc();
                        Speed.mc.player.motionX -= (double) (MathHelper.sin(yaw) * 0.1F);
                        Speed.mc.player.motionZ += (double) (MathHelper.cos(yaw) * 0.1F);
                    } else if (Speed.mc.player.onGround && this.mode.getValString().equalsIgnoreCase("OnGround")) {
                        yaw = this.GetRotationYawForCalc();
                        Speed.mc.player.motionX -= (double) (MathHelper.sin(yaw) * 0.13F);
                        Speed.mc.player.motionZ += (double) (MathHelper.cos(yaw) * 0.13F);
                        Speed.mc.player.connection.sendPacket(new Position(Speed.mc.player.posX, Speed.mc.player.posY + 0.4D, Speed.mc.player.posZ, false));
                    }
                }

                if (Speed.mc.gameSettings.keyBindJump.isKeyDown() && Speed.mc.player.onGround) {
                    Speed.mc.player.motionY = 0.4050000011920929D;
                }

                if (Speed.mc.player.movementInput.moveForward == 0.0F && Speed.mc.player.movementInput.moveStrafe == 0.0F) {
                    Speed.mc.player.motionX *= 0.2D;
                    Speed.mc.player.motionZ *= 0.2D;
                }

            }
        }
    }

    public void onEnable() {
        this.Timer = (TimerMod) RiftMod.fevents.moduleManager.getModule("Timer");
    }

    public void onDisable() {
        if (this.usetimer.getValBoolean() && this.Timer != null) {
            this.Timer.SetOverrideSpeed(1.0F);
        }

    }

    private float GetRotationYawForCalc() {
        float rotationYaw = Speed.mc.player.rotationYaw;

        if (Speed.mc.player.moveForward < 0.0F) {
            rotationYaw += 180.0F;
        }

        float n = 1.0F;

        if (Speed.mc.player.moveForward < 0.0F) {
            n = -0.5F;
        } else if (Speed.mc.player.moveForward > 0.0F) {
            n = 0.5F;
        }

        if (Speed.mc.player.moveStrafing > 0.0F) {
            rotationYaw -= 90.0F * n;
        }

        if (Speed.mc.player.moveStrafing < 0.0F) {
            rotationYaw += 90.0F * n;
        }

        return rotationYaw * 0.017453292F;
    }
}
