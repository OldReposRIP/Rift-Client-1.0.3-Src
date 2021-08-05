package org.yaml.snakeyaml.parser;

import org.yaml.snakeyaml.events.Event;

public interface Parser {

    boolean checkEvent(Event.ID event_id);

    Event peekEvent();

    Event getEvent();
}
