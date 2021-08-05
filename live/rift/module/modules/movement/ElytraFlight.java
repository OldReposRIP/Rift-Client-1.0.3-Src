package live.rift.module.modules.movement;

import java.util.ArrayList;
import java.util.function.Predicate;
import live.rift.event.AlpineEvent;
import live.rift.event.events.EventPlayerTravel;
import live.rift.event.events.PacketEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class ElytraFlight extends Module {

    public Setting glide = new Setting("Glide", this, true);
    public Setting speed = new Setting("Speed", this, 18.0D, 1.0D, 50.0D, false);
    public Setting up;
    public Setting takeOff;
    public Setting chestSwitch;
    public Setting strict;
    public ArrayList upModes = new ArrayList();
    @EventHandler
    public Listener playerMovelListener = new Listener((e) -> {
        if (this.isFlying()) {
            e.cancel();
        }

    }, new Predicate[0]);
    @EventHandler
    public Listener packetListener = new Listener((e) -> {
        if (e.getPacket() instanceof CPacketPlayer && this.strict.getValBoolean() && !this.up.getValString().equalsIgnoreCase("Glide") && ElytraFlight.mc.player.rotationPitch < 10.0F) {
            CPacketPlayer p = (CPacketPlayer) e.getPacket();

            p.pitch = this.pitch;
        }

        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook p1 = (SPacketPlayerPosLook) e.getPacket();

            if (e.getEra() == AlpineEvent.Era.PRE) {
                p1.pitch = ElytraFlight.mc.player.rotationPitch;
                p1.yaw = ElytraFlight.mc.player.rotationYaw;
            }
        }

    }, new Predicate[0]);
    public boolean didTakeOff = false;
    public float pitch = 0.0F;
    public int boostTicks = 0;

    public ElytraFlight() {
        super("ElytraFly", 0, Category.MOVEMENT);
        this.up = new Setting("Up Mode", this, "Glide", this.upModes);
        this.upModes.add("Glide");
        this.upModes.add("Direct (WIP)");
        this.upModes.add("None");
        this.takeOff = new Setting("Smart TakeOff", this, true);
        this.chestSwitch = new Setting("Remove Wings OnGround", this, true);
        this.strict = new Setting("NCP Strict", this, true);
    }

    public void onEnable() {
        this.didTakeOff = false;
    }

    public void onUpdate() {
        if (!this.nullCheck()) {
            if (!ElytraFlight.mc.player.isElytraFlying()) {
                this.didTakeOff = false;
            }

            if (this.shouldTakeOff()) {
                ElytraFlight.mc.timer.tickLength = 300.0F;
                ElytraFlight.mc.player.connection.sendPacket(new CPacketEntityAction(ElytraFlight.mc.player, Action.START_FALL_FLYING));
            }

            if (ElytraFlight.mc.player.isElytraFlying() && !this.didTakeOff) {
                this.didTakeOff = true;
            }

            if ((this.didTakeOff || ElytraFlight.mc.player.onGround && ElytraFlight.mc.timer.tickLength != 50.0F) && ElytraFlight.mc.timer.tickLength != 50.0F) {
                ElytraFlight.mc.timer.tickLength = 50.0F;
            }

            if (this.isFlying()) {
                float strafeDeg = 90.0F * ElytraFlight.mc.player.movementInput.moveStrafe;

                strafeDeg *= ElytraFlight.mc.player.movementInput.moveForward != 0.0F ? ElytraFlight.mc.player.movementInput.moveForward * 0.5F : 1.0F;
                float yaw = ElytraFlight.mc.player.rotationYaw - strafeDeg;

                yaw -= (float) (ElytraFlight.mc.player.movementInput.moveForward < 0.0F ? 180 : 0);
                if (ElytraFlight.mc.player.rotationPitch >= 0.0F) {
                    ElytraFlight.mc.player.setVelocity(ElytraFlight.mc.player.motionX, -0.003D, ElytraFlight.mc.player.motionZ);
                }

                if (ElytraFlight.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    ElytraFlight.mc.player.setVelocity(ElytraFlight.mc.player.motionX, -1.2D, ElytraFlight.mc.player.motionZ);
                } else {
                    this.setSpeed(Math.toRadians((double) yaw), ElytraFlight.mc.player.rotationPitch < -10.0F);
                }
            }

        }
    }

    public void onDisable() {
        ElytraFlight.mc.timer.tickLength = 50.0F;
    }

    public boolean shouldTakeOff() {
        ItemStack s = (ItemStack) ElytraFlight.mc.player.inventoryContainer.getInventory().get(6);

        return !ElytraFlight.mc.player.isElytraFlying() && !ElytraFlight.mc.player.onGround && s.getItem() instanceof ItemElytra && !this.didTakeOff;
    }

    public boolean nullCheck() {
        return ElytraFlight.mc.player == null || ElytraFlight.mc.world == null;
    }

    public boolean isFlying() {
        return this.didTakeOff && ElytraFlight.mc.player.isElytraFlying();
    }

    public void setSpeed(double yaw, boolean boost) {
        double currentSpeed = Math.sqrt(ElytraFlight.mc.player.motionX * ElytraFlight.mc.player.motionX + ElytraFlight.mc.player.motionZ * ElytraFlight.mc.player.motionZ);
        double s = 0.0D;

        if (boost && this.up.getValString().equalsIgnoreCase("Glide")) {
            if ((ElytraFlight.mc.gameSettings.keyBindForward.isKeyDown() || ElytraFlight.mc.gameSettings.keyBindBack.isKeyDown() || ElytraFlight.mc.gameSettings.keyBindLeft.isKeyDown() || ElytraFlight.mc.gameSettings.keyBindRight.isKeyDown()) && ElytraFlight.mc.player.rotationPitch < 0.0F) {
                this.doUpwardFlight(currentSpeed, yaw);
            }

        } else {
            if (this.strict.getValBoolean()) {
                this.pitch = ElytraFlight.mc.player.rotationPitch <= 10.0F ? 102.0F : ElytraFlight.mc.player.rotationPitch;
            }

            s = this.speed.getValDouble() / 10.0D;
            if (!ElytraFlight.mc.gameSettings.keyBindForward.isKeyDown() && !ElytraFlight.mc.gameSettings.keyBindBack.isKeyDown() && !ElytraFlight.mc.gameSettings.keyBindLeft.isKeyDown() && !ElytraFlight.mc.gameSettings.keyBindRight.isKeyDown()) {
                ElytraFlight.mc.player.setVelocity(0.0D, -0.003D, 0.0D);
            } else {
                ElytraFlight.mc.player.setVelocity(Math.sin(-yaw) * s, -0.003D, Math.cos(yaw) * s);
            }

        }
    }

    public void doUpwardFlight(double c, double yaw) {
        double multipliedSpeed = 0.128D * Math.min(this.speed.getValDouble() / 10.0D, 2.0D);
        float strictPitch = (float) Math.toDegrees(Math.asin((multipliedSpeed - Math.sqrt(multipliedSpeed * multipliedSpeed - 0.0348D)) / 0.12D));
        float basePitch = this.strict.getValBoolean() && strictPitch < 20.0F ? -strictPitch : -20.0F;
        float targetPitch = ElytraFlight.mc.player.rotationPitch < 0.0F ? Math.max(ElytraFlight.mc.player.rotationPitch * 110.0F / 90.0F - -20.0F, -90.0F) : -20.0F;

        this.pitch = this.pitch <= basePitch && this.boostTicks > 2 ? (this.pitch < targetPitch ? Math.max(this.pitch + 17.0F, targetPitch) : Math.max(this.pitch - 17.0F, targetPitch)) : basePitch;
        double pitch = Math.toRadians((double) this.pitch);
        double targetMotionX = Math.sin(-yaw) * Math.sin(-pitch);
        double targetMotionZ = Math.cos(yaw) * Math.sin(-pitch);
        double targetSpeed = Math.sqrt(targetMotionX * targetMotionX + targetMotionZ * targetMotionZ);
        double upSpeed = c * Math.sin(-pitch) * 0.04D;
        double fallSpeed = Math.cos(pitch) * Math.cos(pitch) * 0.06D - 0.08D;

        ElytraFlight.mc.player.motionX -= upSpeed * targetMotionX / targetSpeed - (targetMotionX / targetSpeed * c - ElytraFlight.mc.player.motionX) * 0.1D;
        ElytraFlight.mc.player.motionY += upSpeed * 3.2D + fallSpeed;
        ElytraFlight.mc.player.motionZ -= upSpeed * targetMotionZ / targetSpeed - (targetMotionZ / targetSpeed * c - ElytraFlight.mc.player.motionZ) * 0.1D;
        ElytraFlight.mc.player.motionX *= 0.99D;
        ElytraFlight.mc.player.motionY *= 0.98D;
        ElytraFlight.mc.player.motionZ *= 0.99D;
    }
}
