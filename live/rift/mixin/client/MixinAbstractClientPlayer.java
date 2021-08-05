package live.rift.mixin.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
    value = { AbstractClientPlayer.class},
    priority = Integer.MAX_VALUE
)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer {

}
