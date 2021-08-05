package live.rift.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtil {

    static Minecraft mc = Minecraft.getMinecraft();

    public static int getItem(Item i) {
        if (InventoryUtil.mc.player == null) {
            return -1;
        } else {
            for (int x = 0; x < InventoryUtil.mc.player.inventoryContainer.getInventory().size(); ++x) {
                if (x != 0 && x != 5 && x != 6 && x != 7 && x != 8) {
                    ItemStack s = (ItemStack) InventoryUtil.mc.player.inventoryContainer.getInventory().get(x);

                    if (!s.isEmpty() && s.getItem().equals(i)) {
                        return x;
                    }
                }
            }

            return -1;
        }
    }
}
