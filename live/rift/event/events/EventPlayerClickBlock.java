package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class EventPlayerClickBlock extends AlpineEvent {

    public BlockPos Location;
    public EnumFacing Facing;

    public EventPlayerClickBlock(BlockPos loc, EnumFacing face) {
        this.Location = loc;
        this.Facing = face;
    }
}
