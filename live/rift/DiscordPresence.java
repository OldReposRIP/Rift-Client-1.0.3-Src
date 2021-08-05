package live.rift;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import live.rift.command.commands.Status;
import net.minecraft.client.Minecraft;

public class DiscordPresence {

    public static DiscordRichPresence presence = new DiscordRichPresence();
    private static boolean hasStarted = false;
    public static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    private static String details;
    private static String state;

    public static void start() {
        RiftMod.logger.info("Starting Discord RPC");
        if (!DiscordPresence.hasStarted) {
            DiscordPresence.hasStarted = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();

            handlers.disconnected = (var1, var2) -> {
                RiftMod.logger.info("Discord RPC disconnected, var1: " + i + ", var2: " + s);
            };
            DiscordPresence.rpc.Discord_Initialize("686330452503560391", handlers, true, "");
            DiscordPresence.presence.startTimestamp = System.currentTimeMillis() / 1000L;
            setRpcFromSettings();
            (new Thread(DiscordPresence::setRpcFromSettingsNonInt, "Discord-RPC-Callback-Handler")).start();
            RiftMod.logger.info("Discord RPC initialised successfully");
        }
    }

    private static void setRpcFromSettingsNonInt() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DiscordPresence.rpc.Discord_RunCallbacks();
                DiscordPresence.details = "RIFT b1.0.3";
                DiscordPresence.state = Status.statusParsed != null ? Status.statusParsed : "Based Rift User Hasn\'t set a status yet";
                DiscordPresence.presence.details = DiscordPresence.details;
                DiscordPresence.presence.state = DiscordPresence.state;
                DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            try {
                Thread.sleep(4000L);
            } catch (InterruptedException interruptedexception) {
                interruptedexception.printStackTrace();
            }
        }

    }

    private static void setRpcFromSettings() {
        DiscordPresence.details = "RIFT b1.0.3";
        DiscordPresence.state = Status.statusParsed != null ? Status.statusParsed : "Based Rift User Hasn\'t set a status yet";
        DiscordPresence.presence.details = DiscordPresence.details;
        DiscordPresence.presence.state = DiscordPresence.state;
        DiscordPresence.presence.largeImageKey = "rift";
        DiscordPresence.presence.largeImageText = ":^)";
        if (Minecraft.getMinecraft().player != null) {
            DiscordPresence.presence.smallImageKey = Minecraft.getMinecraft().player.getName().toLowerCase();
        }

        DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
    }
}
