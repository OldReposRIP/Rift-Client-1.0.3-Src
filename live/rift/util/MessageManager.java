package live.rift.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class MessageManager {

    public static void sendClientMessage(String s) {
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§l§7[§l§8RIFT§l§7]§r " + s));
    }
}
