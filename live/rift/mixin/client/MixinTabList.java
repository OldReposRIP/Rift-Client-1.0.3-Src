package live.rift.mixin.client;

import java.util.List;
import live.rift.module.modules.render.Tablist;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = { GuiPlayerTabOverlay.class},
    priority = Integer.MAX_VALUE
)
public class MixinTabList {

    @Redirect(
        method = { "renderPlayerlist"},
        at =             @At(
                value = "INVOKE",
                target = "Ljava/util/List;subList(II)Ljava/util/List;"
            )
    )
    public List subList(List list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, Tablist.INST.isEnabled() ? Math.min(240, list.size()) : toIndex);
    }

    @Inject(
        method = { "getPlayerName"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable returnable) {
        if (Tablist.INST.isEnabled()) {
            returnable.cancel();
            returnable.setReturnValue(Tablist.getName(networkPlayerInfoIn));
        }

    }
}
