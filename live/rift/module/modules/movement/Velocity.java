package live.rift.module.modules.movement;

import java.util.function.Predicate;
import live.rift.event.events.PacketEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public class Velocity extends Module {

    @EventHandler
    private Listener receiveListener = new Listener((event) -> {
        if (event.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity) event.getPacket()).getEntityID() == Velocity.mc.player.getEntityId()) {
            event.cancel();
        }

        if (event.getPacket() instanceof SPacketExplosion) {
            event.cancel();
        }

    }, new Predicate[0]);

    public Velocity() {
        super("Velocity", 0, Category.MOVEMENT);
    }
}
