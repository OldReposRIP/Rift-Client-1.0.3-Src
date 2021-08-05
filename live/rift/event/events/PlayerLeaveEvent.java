package live.rift.event.events;

import live.rift.event.AlpineEvent;

public class PlayerLeaveEvent extends AlpineEvent {

    private final String name;

    public PlayerLeaveEvent(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }
}
