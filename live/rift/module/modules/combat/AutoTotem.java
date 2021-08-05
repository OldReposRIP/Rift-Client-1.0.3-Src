package live.rift.module.modules.combat;

import java.util.ArrayList;
import live.rift.message.Messages;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class AutoTotem extends Module {

    Setting priority;
    public ArrayList priorities = new ArrayList();
    Setting health = new Setting("Health", this, 15.0D, 1.0D, 36.0D, true);
    Setting holeHealth = new Setting("HoleHealth", this, 2.0D, 1.0D, 36.0D, true);
    Setting messages;
    Setting allowGap;
    Setting holeGap;
    AutoTotem.State s;
    int totems = 0;

    public AutoTotem() {
        super("AutoTotem", 0, Category.COMBAT);
        this.priorities.add("Totem");
        this.priorities.add("Crystal");
        this.priorities.add("Gapple");
        this.priorities.add("Sword");
        this.priorities.add("Pickaxe");
        this.priority = new Setting("Priority", this, "Totem", this.priorities);
        this.allowGap = new Setting("AllowGap", this, true);
        this.holeGap = new Setting("HoleGap", this, false);
        this.messages = new Setting("Alerts", this, false);
    }

    public void switchOffhand(String i) {
        Item prioItem = null;

        prioItem = this.getItemVal(i);
        int slot = InventoryUtil.getItem(prioItem);

        if (slot != -1) {
            if (this.findTotems(prioItem) == -1 || AutoTotem.mc.currentScreen instanceof GuiContainer) {
                return;
            }

            if (this.messages.getValBoolean()) {
                Messages.sendChatMessage("AutoTotem equipped you with a " + i);
            }

            this.s = new AutoTotem.State(slot, this.getItemVal(i));
            AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, AutoTotem.mc.player);
            AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, AutoTotem.mc.player);
            AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, AutoTotem.mc.player);
            AutoTotem.mc.playerController.updateController();
        } else {
            int tslot = InventoryUtil.getItem(Items.TOTEM_OF_UNDYING);

            if (tslot != -1) {
                if (this.findTotems(Items.TOTEM_OF_UNDYING) == -1 || AutoTotem.mc.currentScreen instanceof GuiContainer) {
                    return;
                }

                this.s = new AutoTotem.State(slot, this.getItemVal("Totem"));
                if (this.messages.getValBoolean()) {
                    Messages.sendChatMessage("Fallback Emergency Totem equipped!");
                }

                AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, tslot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.windowClick(AutoTotem.mc.player.inventoryContainer.windowId, tslot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.updateController();
            }
        }

    }

    public void onUpdate() {
        if (AutoTotem.mc.player != null) {
            this.totems = AutoTotem.mc.player.inventory.mainInventory.stream().filter(test<invokedynamic>()).mapToInt(applyAsInt<invokedynamic>()).sum();
            if (AutoTotem.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
                ++this.totems;
            }

            this.modInfo = String.valueOf(this.totems) != null ? String.valueOf(this.totems) : "0";
            if (InventoryUtil.getItem(this.getItemVal(this.priority.getValString())) == -1) {
                if (InventoryUtil.getItem(Items.TOTEM_OF_UNDYING) != -1 && !AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                    this.switchOffhand("Totem");
                }
            } else if ((double) (AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount()) >= (this.isInHole(AutoTotem.mc.player) ? this.holeHealth.getValDouble() : this.health.getValDouble()) && !AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(this.getItemVal(this.priority.getValString()))) {
                if (!this.allowGap.getValBoolean() && (!this.holeGap.getValBoolean() || (double) (AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount()) < this.holeHealth.getValDouble())) {
                    this.switchOffhand(this.priority.getValString());
                } else if (!AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.GOLDEN_APPLE)) {
                    this.switchOffhand(this.priority.getValString());
                }
            }

            if ((double) (AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount()) <= (this.isInHole(AutoTotem.mc.player) ? this.holeHealth.getValDouble() : this.health.getValDouble()) && !AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                this.switchOffhand("Totem");
            }

            if (this.holeGap.getValBoolean() && this.isInHole(AutoTotem.mc.player) && (double) (AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount()) >= this.holeHealth.getValDouble() && !AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.GOLDEN_APPLE)) {
                this.switchOffhand("Gapple");
            }

        }
    }

    public Item getItemVal(String i) {
        Item prioItem = null;
        byte b0 = -1;

        switch (i.hashCode()) {
        case -1582753002:
            if (i.equals("Crystal")) {
                b0 = 1;
            }
            break;

        case 80307677:
            if (i.equals("Sword")) {
                b0 = 3;
            }
            break;

        case 80997281:
            if (i.equals("Totem")) {
                b0 = 0;
            }
            break;

        case 1086624557:
            if (i.equals("Pickaxe")) {
                b0 = 4;
            }
            break;

        case 2125698931:
            if (i.equals("Gapple")) {
                b0 = 2;
            }
        }

        switch (b0) {
        case 0:
            prioItem = Items.TOTEM_OF_UNDYING;
            break;

        case 1:
            prioItem = Items.END_CRYSTAL;
            break;

        case 2:
            prioItem = Items.GOLDEN_APPLE;
            break;

        case 3:
            prioItem = Items.DIAMOND_SWORD;
            break;

        case 4:
            prioItem = Items.DIAMOND_PICKAXE;
        }

        return prioItem;
    }

    public int findTotems(Item item) {
        if (AutoTotem.mc.player == null) {
            return -1;
        } else {
            int t = -1;

            for (int i = 0; i < 45; ++i) {
                if (AutoTotem.mc.player.inventory.getStackInSlot(i).getItem() == item) {
                    t = i;
                    break;
                }
            }

            return t;
        }
    }

    public boolean isInHole(Entity e) {
        int holeBlocks = 0;
        int lowerBlocks = 0;
        BlockPos pos = new BlockPos(Math.floor(e.getPositionVector().x), Math.floor(e.getPositionVector().y), Math.floor(e.getPositionVector().z));
        BlockPos[] offset = new BlockPos[] { new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
        BlockPos[] lower = new BlockPos[] { new BlockPos(1, -1, 0), new BlockPos(-1, -1, 0), new BlockPos(0, -1, 1), new BlockPos(0, -1, -1)};
        int i = -1;
        BlockPos[] ablockpos = offset;
        int i = offset.length;

        int j;
        BlockPos l;
        BlockPos finalp;
        Block b;

        for (j = 0; j < i; ++j) {
            l = ablockpos[j];
            ++i;
            finalp = pos.add(l);
            b = AutoTotem.mc.world.getBlockState(finalp.down()).getBlock();
            Block b1 = AutoTotem.mc.world.getBlockState(finalp).getBlock();

            if (b1 == Blocks.OBSIDIAN || b1 == Blocks.BEDROCK) {
                ++holeBlocks;
                if (b == Blocks.AIR) {
                    --lowerBlocks;
                }
            }
        }

        ablockpos = lower;
        i = lower.length;

        for (j = 0; j < i; ++j) {
            l = ablockpos[j];
            finalp = pos.add(l);
            b = AutoTotem.mc.world.getBlockState(finalp).getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK) {
                ++lowerBlocks;
            }
        }

        if (holeBlocks < lowerBlocks) {
            return false;
        } else if (!e.onGround) {
            return false;
        } else {
            return holeBlocks >= 4 || holeBlocks + lowerBlocks == 4;
        }
    }

    private static boolean lambda$onUpdate$0(ItemStack itemStack) {
        return itemStack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    public class State {

        int slot;
        Item item;

        public State(int slot, Item item) {
            this.slot = slot;
            this.item = item;
        }

        public int getSlot() {
            return this.slot;
        }

        public Item getItem() {
            return this.item;
        }
    }
}
