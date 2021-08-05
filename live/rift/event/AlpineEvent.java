package live.rift.event;

import me.zero.alpine.type.Cancellable;
import net.minecraft.client.Minecraft;

public class AlpineEvent extends Cancellable {

    private AlpineEvent.Era era;
    final float partialTicks;

    public AlpineEvent() {
        this.era = AlpineEvent.Era.PRE;
        this.partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
    }

    public AlpineEvent.Era getEra() {
        return this.era;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public static enum Era {

        PRE, PERI, POST;
    }
}
