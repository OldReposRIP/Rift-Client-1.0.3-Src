package live.rift.module.modules.misc;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;
import live.rift.event.events.PacketEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.EventHook;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;

public class CustomChat extends Module {

    Setting skid = new Setting("AutoSkid", this, false);
    public ArrayList skid1 = new ArrayList();
    public ArrayList skid2 = new ArrayList();
    public static final Random rand = new Random();
    @EventHandler
    public Listener listener = new Listener((event) -> {
        if (event.getPacket() instanceof CPacketChatMessage && this.isEnabled()) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();

            if (s.startsWith("/") && !s.startsWith("/w ") && !s.startsWith("/r ") && !s.startsWith("/l ") && !s.startsWith("/msg ")) {
                ((CPacketChatMessage) event.getPacket()).message = s;
            } else {
                if (!this.skid.getValBoolean()) {
                    s = s + " ⩺ �?�ᵢꜰⵜ";
                }

                if (this.skid.getValBoolean()) {
                    s = s + " �?? " + (String) this.skid1.get(CustomChat.rand.nextInt(this.skid1.size())) + (String) this.skid2.get(CustomChat.rand.nextInt(this.skid2.size()));
                }

                ((CPacketChatMessage) event.getPacket()).message = s;
            }
        }

    }, new Predicate[0]);

    public CustomChat() {
        super("CustomChat", 0, Category.MISC);
    }

    public void onEnable() {
        this.skid1.add("ᴡᴜʀѕᴛ");
        this.skid1.add("ɴᴜᴛ");
        this.skid1.add("ʀᴜѕʜᴇʀ");
        this.skid1.add("ᴋᴀ�?ɪ");
        this.skid1.add("ᴄᴀᴘ");
        this.skid1.add("ѕᴇх");
        this.skid1.add("ᴘʜ�?ʙ�?ѕ");
        this.skid1.add("ꜰᴜᴛᴜʀᴇ");
        this.skid2.add(" ʙʟᴜᴇ");
        this.skid2.add("ɢ�?ᴅ.ᴄᴄ");
        this.skid2.add("ɢ�?ᴅᴅᴇѕѕ.ɢɢ");
        this.skid2.add("ʜᴀᴄᴋ");
        this.skid2.add("ᴡᴀʀᴇ");
        this.skid2.add(" ᴄʟɪᴇɴᴛ");
        this.skid2.add(" �?�?ᴅ");
        this.skid2.add(" ᴘʟᴜѕ");
    }
}
