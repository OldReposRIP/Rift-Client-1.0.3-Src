package live.rift.module.modules.combat;

import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.BlockInteractionHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class SelfWeb extends Module {

    private BlockPos self;
    int webslot = -1;
    public Setting delay = new Setting("Delay", this, 3.0D, 2.0D, 25.0D, true);
    public Setting autoswitch = new Setting("AutoSwitch", this, true);
    public Setting disable = new Setting("Disable after place", this, false);

    public SelfWeb() {
        super("SelfWeb", 0, Category.COMBAT);
    }

    public void onUpdate() {
        int delaysplit;

        for (delaysplit = 0; delaysplit < 9 && this.webslot == -1; ++delaysplit) {
            ItemStack stack = SelfWeb.mc.player.inventory.getStackInSlot(delaysplit);

            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block == Blocks.WEB) {
                    this.webslot = delaysplit;
                }
            }
        }

        delaysplit = (int) this.delay.getValDouble() / 2;
        if (SelfWeb.mc.player.onGround) {
            this.self = new BlockPos(SelfWeb.mc.player.posX, SelfWeb.mc.player.posY, SelfWeb.mc.player.posZ);
            if (SelfWeb.mc.world.getBlockState(this.self).getBlock() != Blocks.WEB) {
                if (this.autoswitch.getValBoolean()) {
                    this.SwitchHandToItemIfNeed(Item.getItemById(30));
                    if (SelfWeb.mc.player.ticksExisted % delaysplit == 0) {
                        if (((ItemBlock) SelfWeb.mc.player.getHeldItemMainhand().getItem()).getBlock() == Blocks.WEB) {
                            BlockInteractionHelper.placeBlockScaffold(this.self, Integer.valueOf(delaysplit));
                        }

                        if (this.disable.getValBoolean()) {
                            this.disable();
                        }
                    }

                    if (this.webslot == -1 && SelfWeb.mc.world.getBlockState(this.self).getBlock() == Blocks.WEB) {
                        Messages.sendChatMessage(" Web not found in hotbar");
                        this.disable();
                    }
                } else if (!this.autoswitch.getValBoolean() && SelfWeb.mc.player.getHeldItemMainhand().getItem() == Item.getItemById(30) || SelfWeb.mc.player.getHeldItemMainhand().getItem() == Item.getItemById(30)) {
                    if (SelfWeb.mc.player.ticksExisted % delaysplit == 0) {
                        BlockInteractionHelper.placeBlockScaffold(this.self, Integer.valueOf(delaysplit));
                        if (this.disable.getValBoolean()) {
                            this.disable();
                        }
                    }

                    if (this.webslot == -1 && SelfWeb.mc.world.getBlockState(this.self).getBlock() == Blocks.WEB) {
                        Messages.sendChatMessage(" Web not found in hotbar");
                        this.disable();
                    }
                }
            }
        }

    }

    private boolean SwitchHandToItemIfNeed(Item item) {
        if (SelfWeb.mc.player.getHeldItemMainhand().getItem() != item && SelfWeb.mc.player.getHeldItemOffhand().getItem() != item) {
            for (int l_I = 0; l_I < 9; ++l_I) {
                ItemStack l_Stack = SelfWeb.mc.player.inventory.getStackInSlot(l_I);

                if (l_Stack != ItemStack.EMPTY && l_Stack.getItem() == item) {
                    SelfWeb.mc.player.inventory.currentItem = l_I;
                    SelfWeb.mc.playerController.updateController();
                    return true;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
