package live.rift.mixin.client;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { NetHandlerPlayClient.class},
    priority = Integer.MAX_VALUE
)
public class MixinNetHandlerPlayClient {

    @Shadow
    private final Map playerInfoMap = Maps.newHashMap();

    @Inject(
        method = { "Lnet/minecraft/client/network/NetHandlerPlayClient;handlePlayerListItem(Lnet/minecraft/network/play/server/SPacketPlayerListItem;)V"},
        at = {             @At("HEAD")}
    )
    public void preHandlePlayerListItem(SPacketPlayerListItem listItem, CallbackInfo callbackInfo) {}
}
