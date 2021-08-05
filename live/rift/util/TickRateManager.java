package live.rift.util;

import java.util.function.Predicate;
import live.rift.RiftMod;
import live.rift.event.events.PacketEvent;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;

public class TickRateManager {

    private long prevTime = -1L;
    private float[] ticks = new float[20];
    private int currentTick;
    @EventHandler
    private Listener PacketEvent = new Listener((p_Event) -> {
        // $FF: Couldn't be decompiled
    }, new Predicate[0]);

    public TickRateManager() {
        int i = 0;

        for (int len = this.ticks.length; i < len; ++i) {
            this.ticks[i] = 0.0F;
        }

        RiftMod.EVENT_BUS.subscribe((Object) this);
    }

    public float getTickRate() {
        int tickCount = 0;
        float tickRate = 0.0F;

        for (int i = 0; i < this.ticks.length; ++i) {
            float tick = this.ticks[i];

            if (tick > 0.0F) {
                tickRate += tick;
                ++tickCount;
            }
        }

        return MathHelper.clamp(tickRate / (float) tickCount, 0.0F, 20.0F);
    }

    public static TickRateManager Get() {
        return RiftMod.GetTickRateManager();
    }
}
