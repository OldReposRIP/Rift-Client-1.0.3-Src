package live.rift.message;

import net.minecraft.client.Minecraft;

public class Messages {

    public static void sendChatMessage(String message) {
        sendRawChatMessage("&0[&fRift&0] &r" + message);
    }

    public static void sendMessage(String message) {
        sendRawChatMessage(message);
    }

    public static void sendStringChatMessage(String[] messages) {
        sendChatMessage("");
        String[] astring = messages;
        int i = messages.length;

        for (int j = 0; j < i; ++j) {
            String s = astring[j];

            sendRawChatMessage(s);
        }

    }

    public static void sendRawChatMessage(String message) {
        if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null) {
            Minecraft.getMinecraft().player.sendMessage(new ChatMessage(message));
        }

    }
}
