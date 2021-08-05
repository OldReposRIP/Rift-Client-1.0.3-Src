package live.rift.event.events;

import live.rift.event.AlpineEvent;

public class PlayerJoinEvent extends AlpineEvent {

    private final String name;

    public PlayerJoinEvent(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }
}
