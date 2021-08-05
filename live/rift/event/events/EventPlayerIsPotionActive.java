package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.potion.Potion;

public class EventPlayerIsPotionActive extends AlpineEvent {

    public Potion potion;

    public EventPlayerIsPotionActive(Potion p_Potion) {
        this.potion = p_Potion;
    }
}
