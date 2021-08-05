package live.rift.module.modules.movement;

import live.rift.module.Category;
import live.rift.module.Module;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", 0, Category.MOVEMENT);
    }

    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent e) {
        if (NoSlow.mc.player.isHandActive() && !NoSlow.mc.player.isRiding()) {
            MovementInput movementinput = e.getMovementInput();

            movementinput.moveStrafe *= 5.0F;
            movementinput = e.getMovementInput();
            movementinput.moveForward *= 5.0F;
        }

    }
}
