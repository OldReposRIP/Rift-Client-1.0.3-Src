package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.client.entity.EntityPlayerSP;

public class EventMotionUpdate extends AlpineEvent {

    private AlpineEvent.Era type;
    private EntityPlayerSP player;
    private float yaw;
    private float pitch;
    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public EventMotionUpdate(AlpineEvent.Era type, EntityPlayerSP player, float yaw, float pitch, double x, double y, double z, boolean onGround) {
        this.type = type;
        this.player = player;
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    public EventMotionUpdate() {
        this.type = AlpineEvent.Era.POST;
    }

    public void setType(AlpineEvent.Era type) {
        this.type = type;
    }

    public void setPlayer(EntityPlayerSP player) {
        this.player = player;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public AlpineEvent.Era getType() {
        return this.type;
    }

    public EntityPlayerSP getPlayer() {
        return this.player;
    }
}
