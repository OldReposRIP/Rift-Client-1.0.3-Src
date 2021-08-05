package live.rift.event.events;

import live.rift.event.AlpineEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class EventPlayerDamageBlock extends AlpineEvent {

    private BlockPos BlockPos;
    private EnumFacing Direction;

    public EventPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        this.BlockPos = posBlock;
        this.setDirection(directionFacing);
    }

    public BlockPos getPos() {
        return this.BlockPos;
    }

    public EnumFacing getDirection() {
        return this.Direction;
    }

    public void setDirection(EnumFacing direction) {
        this.Direction = direction;
    }
}
