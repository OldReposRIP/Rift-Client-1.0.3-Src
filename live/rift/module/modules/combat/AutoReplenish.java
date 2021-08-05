package live.rift.module.modules.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class AutoReplenish extends Module {

    public Setting delay = new Setting("Delay", this, 1.0D, 1.0D, 20.0D, true);
    public Setting threshold = new Setting("Threshold", this, 1.0D, 1.0D, 20.0D, true);
    public Setting shift = new Setting("ShiftClick", this, true);
    private int delayStep = 0;

    public AutoReplenish() {
        super("AutoReplenish", 0, Category.COMBAT);
    }

    private static Map getInventory() {
        return getInventorySlots(9, 35);
    }

    private static Map getHotbar() {
        return getInventorySlots(36, 44);
    }

    public static Map getInventorySlots(int current, int last) {
        HashMap fullInventorySlots;

        for (fullInventorySlots = new HashMap(); current <= last; ++current) {
            fullInventorySlots.put(Integer.valueOf(current), AutoReplenish.mc.player.inventoryContainer.getInventory().get(current));
        }

        return fullInventorySlots;
    }

    public void onUpdate() {
        if (AutoReplenish.mc.player != null) {
            if (!(AutoReplenish.mc.currentScreen instanceof GuiContainer)) {
                if ((double) this.delayStep < this.delay.getValDouble()) {
                    ++this.delayStep;
                } else {
                    this.delayStep = 0;
                    Pair slots = this.findReplenishableHotbarSlot();

                    if (slots != null) {
                        int inventorySlot = ((Integer) slots.getKey()).intValue();
                        int hotbarSlot = ((Integer) slots.getValue()).intValue();

                        if (this.shift.getValBoolean()) {
                            AutoReplenish.mc.playerController.windowClick(0, inventorySlot, 0, ClickType.QUICK_MOVE, AutoReplenish.mc.player);
                        } else {
                            AutoReplenish.mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
                            AutoReplenish.mc.playerController.windowClick(0, hotbarSlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
                            AutoReplenish.mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
                        }
                    }
                }
            }
        }
    }

    private Pair findReplenishableHotbarSlot() {
        Pair returnPair = null;
        Iterator iterator = getHotbar().entrySet().iterator();

        while (iterator.hasNext()) {
            Entry hotbarSlot = (Entry) iterator.next();
            ItemStack stack = (ItemStack) hotbarSlot.getValue();

            if (!stack.isEmpty && stack.getItem() != Items.AIR && stack.isStackable() && stack.stackSize < stack.getMaxStackSize() && (double) stack.stackSize <= this.threshold.getValDouble()) {
                int inventorySlot = this.findCompatibleInventorySlot(stack);

                if (inventorySlot != -1) {
                    returnPair = new Pair(Integer.valueOf(inventorySlot), hotbarSlot.getKey());
                }
            }
        }

        return returnPair;
    }

    private int findCompatibleInventorySlot(ItemStack hotbarStack) {
        int inventorySlot = -1;
        int smallestStackSize = 999;
        Iterator iterator = getInventory().entrySet().iterator();

        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            ItemStack inventoryStack = (ItemStack) entry.getValue();

            if (!inventoryStack.isEmpty && inventoryStack.getItem() != Items.AIR && this.isCompatibleStacks(hotbarStack, inventoryStack)) {
                int currentStackSize = ((ItemStack) AutoReplenish.mc.player.inventoryContainer.getInventory().get(((Integer) entry.getKey()).intValue())).stackSize;

                if (smallestStackSize > currentStackSize) {
                    smallestStackSize = currentStackSize;
                    inventorySlot = ((Integer) entry.getKey()).intValue();
                }
            }
        }

        return inventorySlot;
    }

    private boolean isCompatibleStacks(ItemStack stack1, ItemStack stack2) {
        if (!stack1.getItem().equals(stack2.getItem())) {
            return false;
        } else {
            if (stack1.getItem() instanceof ItemBlock && stack2.getItem() instanceof ItemBlock) {
                Block block1 = ((ItemBlock) stack1.getItem()).getBlock();
                Block block2 = ((ItemBlock) stack2.getItem()).getBlock();

                if (!block1.material.equals(block2.material)) {
                    return false;
                }
            }

            return !stack1.getDisplayName().equals(stack2.getDisplayName()) ? false : stack1.getItemDamage() == stack2.getItemDamage();
        }
    }
}
