package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.util.EnumHand;

public class EventSwingArm extends AlpineEvent {

    public EnumHand Hand;

    public EventSwingArm(EnumHand p_Hand) {
        this.Hand = p_Hand;
    }

    public EnumHand getHand() {
        return this.Hand;
    }
}
