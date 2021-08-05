package live.rift.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import live.rift.RiftMod;
import live.rift.event.events.PacketEvent;
import live.rift.event.events.PopTotemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = { NetworkManager.class},
    priority = Integer.MAX_VALUE
)
public class MixinNetworkManager {

    @Inject(
        method = { "sendPacket(Lnet/minecraft/network/Packet;)V"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    private void onSendPacket(Packet packet, CallbackInfo callbackInfo) {
        PacketEvent.Send event = new PacketEvent.Send(packet);

        RiftMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }

    }

    @Inject(
        method = { "channelRead0"},
        at = {             @At("HEAD")},
        cancellable = true
    )
    private void onChannelRead(ChannelHandlerContext context, Packet packet, CallbackInfo callbackInfo) {
        PacketEvent.Receive event = new PacketEvent.Receive(packet);

        RiftMod.EVENT_BUS.post(event);
        if (event != null && event.getPacket() != null && event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35 && Minecraft.getMinecraft().world != null && ((SPacketEntityStatus) event.getPacket()).getEntity(Minecraft.getMinecraft().world) != null) {
            Entity entity = ((SPacketEntityStatus) event.getPacket()).getEntity(Minecraft.getMinecraft().world);

            RiftMod.EVENT_BUS.post(new PopTotemEvent(entity));
        }

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }

    }
}
