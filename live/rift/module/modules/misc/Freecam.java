package live.rift.module.modules.misc;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.function.Predicate;
import live.rift.event.events.PacketEvent;
import live.rift.event.events.PushOutBlockEvent;
import live.rift.event.events.RenderHandEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;

public class Freecam extends Module {

    public Setting speed = new Setting("Speed", this, 1.0D, 0.1D, 5.0D, false);
    public Setting cancelPackets = new Setting("PacketCancel", this, true);
    private double startX;
    private double startY;
    private double startZ;
    private float yaw;
    private float pitch;
    @EventHandler
    private Listener motionEvent = new Listener((event) -> {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            List boxes = Freecam.mc.world.getCollisionBoxes(Freecam.mc.player, Freecam.mc.player.getEntityBoundingBox().expand(0.5D, 0.5D, 0.5D));

            Freecam.mc.player.noClip = !boxes.isEmpty();
            if (!Freecam.mc.player.capabilities.isFlying) {
                Freecam.mc.player.capabilities.isFlying = true;
            }

            if (Freecam.mc.inGameHasFocus) {
                if (Freecam.mc.gameSettings.keyBindJump.isKeyDown()) {
                    Freecam.mc.player.motionY = 0.4D;
                }

                if (Freecam.mc.gameSettings.keyBindSneak.isKeyDown()) {
                    Freecam.mc.player.motionY = -0.4D;
                }
            }

        }
    }, new Predicate[0]);
    @EventHandler
    private Listener moveEvent = new Listener((event) -> {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            if (Freecam.mc.player.moveForward != 0.0F || Freecam.mc.player.moveStrafing != 0.0F) {
                Freecam.mc.player.motionX *= this.speed.getValDouble();
                Freecam.mc.player.motionZ *= this.speed.getValDouble();
            }

            Freecam.mc.player.noClip = true;
        }
    }, new Predicate[0]);
    @EventHandler
    private Listener sendListener = new Listener((event) -> {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            if (this.cancelPackets.getValBoolean() && (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput)) {
                event.cancel();
            }

        }
    }, new Predicate[0]);
    @EventHandler
    private Listener handEvent = new Listener((event) -> {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            event.cancel();
        }
    }, new Predicate[0]);
    @EventHandler
    private Listener pushListener = new Listener((event) -> {
        event.cancel();
    }, new Predicate[0]);

    public Freecam() {
        super("Freecam", 0, Category.MISC);
    }

    public void onEnable() {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            Freecam.mc.renderGlobal.loadRenderers();
            this.startX = Freecam.mc.player.posX;
            this.startY = Freecam.mc.player.posY;
            this.startZ = Freecam.mc.player.posZ;
            this.yaw = Freecam.mc.player.rotationYaw;
            this.pitch = Freecam.mc.player.rotationPitch;
            EntityOtherPlayerMP entity = new EntityOtherPlayerMP(Freecam.mc.world, new GameProfile(Freecam.mc.player.getUniqueID(), Freecam.mc.player.getCommandSenderEntity().getName()));

            Freecam.mc.world.addEntityToWorld(-1337, entity);
            entity.setPositionAndRotation(this.startX, Freecam.mc.player.getEntityBoundingBox().minY, this.startZ, this.yaw, this.pitch);
            entity.setSneaking(Freecam.mc.player.isSneaking());
        }
    }

    public void onDisable() {
        if (Freecam.mc != null && Freecam.mc.player != null && Freecam.mc.world != null && Freecam.mc.renderGlobal != null) {
            Freecam.mc.renderGlobal.loadRenderers();
            Freecam.mc.player.setPositionAndRotation(this.startX, this.startY, this.startZ, this.yaw, this.pitch);
            Freecam.mc.player.noClip = false;
            Freecam.mc.world.removeEntityFromWorld(-1337);
            Freecam.mc.player.capabilities.isFlying = false;
        }
    }
}
