package live.rift.module.modules.hud;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.modules.hud.screen.ConsoleScreen;
import net.minecraft.client.gui.GuiScreen;

public class Console extends Module {

    public Console() {
        super("Console", 0, Category.HUD);
    }

    public void onToggle(boolean state) {
        this.setState(false);
        Console.mc.displayGuiScreen((GuiScreen) null);
        Console.mc.setIngameFocus();
        Console.mc.getSoundHandler().resumeSounds();
        Console.mc.displayGuiScreen(new ConsoleScreen());
    }
}
