package club.minnced.discord.rpc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DiscordRPC extends Library {

    DiscordRPC INSTANCE = (DiscordRPC) Native.loadLibrary("discord-rpc", DiscordRPC.class);
    int DISCORD_REPLY_NO = 0;
    int DISCORD_REPLY_YES = 1;
    int DISCORD_REPLY_IGNORE = 2;

    void Discord_Initialize(@Nonnull String s, @Nullable DiscordEventHandlers discordeventhandlers, boolean flag, @Nullable String s1);

    void Discord_Shutdown();

    void Discord_RunCallbacks();

    void Discord_UpdateConnection();

    void Discord_UpdatePresence(@Nullable DiscordRichPresence discordrichpresence);

    void Discord_ClearPresence();

    void Discord_Respond(@Nonnull String s, int i);

    void Discord_UpdateHandlers(@Nullable DiscordEventHandlers discordeventhandlers);

    void Discord_Register(String s, String s1);

    void Discord_RegisterSteamGame(String s, String s1);
}
