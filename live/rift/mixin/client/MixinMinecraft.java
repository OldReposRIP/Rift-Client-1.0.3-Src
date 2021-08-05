package live.rift.mixin.client;

import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.event.events.EventPlayerJump;
import live.rift.event.events.MiddleClickEvent;
import live.rift.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { Minecraft.class},
    priority = Integer.MAX_VALUE
)
public class MixinMinecraft {

    @Shadow
    public PlayerControllerMP playerController;

    @Inject(
        method = { "runTickKeyboard"},
        at = {             @At(
                value = "INVOKE",
                remap = false,
                target = "Lorg/lwjgl/input/Keyboard;getEventKey()I",
                ordinal = 0,
                shift = At.Shift.BEFORE
            )}
    )
    private void onKeyboard(CallbackInfo callbackInfo) {
        int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

        if (Keyboard.getEventKeyState()) {
            if (Minecraft.getMinecraft().gameSettings != null && i == Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode() && Minecraft.getMinecraft().player != null) {
                EventPlayerJump event = new EventPlayerJump(Minecraft.getMinecraft().player.motionX, Minecraft.getMinecraft().player.motionZ);

                RiftMod.EVENT_BUS.post(event);
            }

            if (RiftMod.fevents.moduleManager != null) {
                Iterator event1 = RiftMod.fevents.moduleManager.modules.iterator();

                while (event1.hasNext()) {
                    Module module = (Module) event1.next();

                    module.onKey(i);
                }
            }
        }

    }

    @Redirect(
        method = { "sendClickBlockToController"},
        at =             @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"
            )
    )
    private boolean isHandActive(EntityPlayerSP player) {
        return false;
    }

    @Redirect(
        method = { "rightClickMouse"},
        at =             @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z"
            )
    )
    private boolean isHittingBlock(PlayerControllerMP playerControllerMP) {
        return false;
    }

    @Inject(
        method = { "middleClickMouse"},
        at = {             @At("HEAD")}
    )
    private void middleClickMouse(CallbackInfo callback) {
        MiddleClickEvent mce = new MiddleClickEvent();

        RiftMod.EVENT_BUS.post(mce);
    }

    @Inject(
        method = { "run"},
        at = {             @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V",
                shift = At.Shift.BEFORE
            )}
    )
    public void displayCrashReport(CallbackInfo _info) {
        RiftMod.save();
    }

    @Inject(
        method = { "shutdown"},
        at = {             @At("HEAD")}
    )
    public void shutdown(CallbackInfo info) {
        RiftMod.save();
    }
}
