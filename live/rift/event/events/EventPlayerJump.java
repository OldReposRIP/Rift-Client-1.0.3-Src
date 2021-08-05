package live.rift.event.events;

import live.rift.event.AlpineEvent;

public class EventPlayerJump extends AlpineEvent {

    public double MotionX;
    public double MotionY;

    public EventPlayerJump(double p_MotionX, double p_MotionY) {
        this.MotionX = p_MotionX;
        this.MotionY = p_MotionY;
    }
}
