package live.rift;

import java.util.Iterator;
import live.rift.command.CommandManager;
import live.rift.config.Configuration;
import live.rift.event.ForgeEvents;
import live.rift.friends.Friends;
import live.rift.gui.ClickGui;
import live.rift.module.Module;
import live.rift.setting.SettingManager;
import live.rift.util.TickRateManager;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(
    modid = "rift",
    name = "RIFT",
    version = "b1.0.3"
)
public class RiftMod {

    public static final String MODID = "rift";
    public static final String NAME = "RIFT";
    public static final String VERSION = "b1.0.3";
    public static final EventBus EVENT_BUS = new EventManager();
    public static SettingManager setmgr;
    public static Logger logger;
    public static ForgeEvents fevents;
    public static ClickGui clickgui;
    public static CommandManager cmdmanager;
    public static Friends friends;
    private static TickRateManager tickRateManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RiftMod.logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        RiftMod.setmgr = new SettingManager();
        MinecraftForge.EVENT_BUS.register(RiftMod.fevents = new ForgeEvents());
        RiftMod.clickgui = new ClickGui();
        RiftMod.cmdmanager = new CommandManager();
        RiftMod.tickRateManager = new TickRateManager();
        RiftMod.logger.info("-RIFT- : PREINIT");
        DiscordPresence.start();
    }

    @EventHandler
    public void init(FMLPostInitializationEvent event) {
        Configuration conf = new Configuration();

        RiftMod.friends = new Friends();
        Display.setTitle("Rift b1.0.3");
        conf.createConfig(RiftMod.fevents.moduleManager, RiftMod.setmgr);
        Iterator iterator = RiftMod.fevents.moduleManager.modules.iterator();

        while (iterator.hasNext()) {
            Module m = (Module) iterator.next();

            conf.loadSettings(m);
        }

        RiftMod.friends.createFriends();
    }

    public static void save() {
        Configuration config = new Configuration();

        config.saveSettings(RiftMod.fevents.moduleManager, RiftMod.setmgr);
        config.savePanels();
        RiftMod.friends.saveFriends();
    }

    public static TickRateManager GetTickRateManager() {
        return RiftMod.tickRateManager;
    }
}
