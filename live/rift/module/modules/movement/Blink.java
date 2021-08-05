package live.rift.module.modules.movement;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;
import live.rift.event.events.PacketEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;

public class Blink extends Module {

    Queue packets = new LinkedList();
    @EventHandler
    public Listener listener = new Listener((event) -> {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketPlayerDigging || event.getPacket() instanceof CPacketAnimation || event.getPacket() instanceof CPacketConfirmTeleport) {
                event.cancel();
                this.packets.add(event.getPacket());
            }

        }
    }, new Predicate[0]);
    private EntityOtherPlayerMP clonedPlayer;

    public Blink() {
        super("Blink", 0, Category.MOVEMENT);
    }

    public void onEnable() {
        if (Blink.mc.player != null) {
            this.clonedPlayer = new EntityOtherPlayerMP(Blink.mc.world, Blink.mc.getSession().getProfile());
            this.clonedPlayer.copyLocationAndAnglesFrom(Blink.mc.player);
            this.clonedPlayer.rotationYawHead = Blink.mc.player.rotationYawHead;
            Blink.mc.world.addEntityToWorld(-100, this.clonedPlayer);
        }

    }

    public void onDisable() {
        while (!this.packets.isEmpty()) {
            Blink.mc.player.connection.sendPacket((Packet) this.packets.poll());
        }

        EntityPlayerSP localPlayer = Blink.mc.player;

        if (localPlayer != null) {
            Blink.mc.world.removeEntityFromWorld(-100);
            this.clonedPlayer = null;
        }

    }

    public void onUpdate() {
        this.modInfo = String.valueOf(this.packets.size());
    }
}
