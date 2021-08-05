package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.entity.Entity;

public class PopTotemEvent extends AlpineEvent {

    private Entity entity;

    public PopTotemEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
