package org.yaml.snakeyaml.events;

import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.Mark;

public final class DocumentStartEvent extends Event {

    private final boolean explicit;
    private final DumperOptions.Version version;
    private final Map tags;

    public DocumentStartEvent(Mark startMark, Mark endMark, boolean explicit, DumperOptions.Version version, Map tags) {
        super(startMark, endMark);
        this.explicit = explicit;
        this.version = version;
        this.tags = tags;
    }

    public boolean getExplicit() {
        return this.explicit;
    }

    public DumperOptions.Version getVersion() {
        return this.version;
    }

    public Map getTags() {
        return this.tags;
    }

    public boolean is(Event.ID id) {
        return Event.ID.DocumentStart == id;
    }
}
