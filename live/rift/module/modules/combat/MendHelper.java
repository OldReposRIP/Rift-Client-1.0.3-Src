package live.rift.module.modules.combat;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class MendHelper extends Module {

    public Setting th = new Setting("Threshold", this, 10.0D, 10.0D, 95.0D, true);
    public Setting auto = new Setting("AutoThrow", this, false);
    public Setting ghost = new Setting("Ghost", this, false);
    public boolean doMend;
    int delay = 0;
    int clicked = 0;
    boolean disable = false;
    int current = -1;

    public MendHelper() {
        super("MendHelper", 0, Category.COMBAT);
    }

    public void onEnable() {
        this.clicked = 0;
        if (MendHelper.mc.player != null) {
            this.current = MendHelper.mc.player.inventory.currentItem;
        }
    }

    public void onUpdate() {
        if (MendHelper.mc.player == null) {
            this.disable();
        }

        EntityEnderCrystal crystal = (EntityEnderCrystal) MendHelper.mc.world.loadedEntityList.stream().filter((e) -> {
            return e instanceof EntityEnderCrystal;
        }).filter((e) -> {
            return e.getDistance(MendHelper.mc.player) <= 6.0F;
        }).map((e) -> {
            return (EntityEnderCrystal) e;
        }).min(Comparator.comparing((e) -> {
            return Float.valueOf(MendHelper.mc.player.getDistance(e));
        })).orElse((Object) null);

        ++this.delay;
        if (crystal != null) {
            this.clicked = 4;
        }

        int xpSlot;
        int i;

        if (this.clicked == 4) {
            this.doMend = false;
            ItemStack l;

            if (!(((ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(8)).getItem() instanceof ItemArmor) && this.delay >= 6) {
                for (xpSlot = 0; xpSlot < MendHelper.mc.player.inventoryContainer.getInventory().size(); ++xpSlot) {
                    if (xpSlot != 0 && xpSlot != 5 && xpSlot != 6 && xpSlot != 7 && xpSlot != 8) {
                        l = (ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(xpSlot);
                        if (!l.isEmpty()) {
                            if (l.getItem() == Items.DIAMOND_BOOTS) {
                                MendHelper.mc.playerController.windowClick(0, xpSlot, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                            }

                            MendHelper.mc.playerController.updateController();
                        }
                    }
                }

                this.delay = 0;
            }

            if (!(((ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(7)).getItem() instanceof ItemArmor) && this.delay >= 6) {
                for (xpSlot = 0; xpSlot < MendHelper.mc.player.inventoryContainer.getInventory().size(); ++xpSlot) {
                    if (xpSlot != 0 && xpSlot != 5 && xpSlot != 6 && xpSlot != 7 && xpSlot != 8) {
                        l = (ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(xpSlot);
                        if (!l.isEmpty()) {
                            if (l.getItem() == Items.DIAMOND_LEGGINGS) {
                                MendHelper.mc.playerController.windowClick(0, xpSlot, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                            }

                            MendHelper.mc.playerController.updateController();
                        }
                    }
                }

                this.delay = 0;
            }

            if (!(((ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(6)).getItem() instanceof ItemArmor) && this.delay >= 6) {
                for (xpSlot = 0; xpSlot < MendHelper.mc.player.inventoryContainer.getInventory().size(); ++xpSlot) {
                    if (xpSlot != 0 && xpSlot != 5 && xpSlot != 6 && xpSlot != 7 && xpSlot != 8) {
                        l = (ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(xpSlot);
                        if (!l.isEmpty()) {
                            if (l.getItem() == Items.DIAMOND_CHESTPLATE) {
                                MendHelper.mc.playerController.windowClick(0, xpSlot, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                            }

                            MendHelper.mc.playerController.updateController();
                        }
                    }
                }

                this.delay = 0;
            }

            if (!(((ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(5)).getItem() instanceof ItemArmor) && this.delay >= 6) {
                for (xpSlot = 0; xpSlot < MendHelper.mc.player.inventoryContainer.getInventory().size(); ++xpSlot) {
                    if (xpSlot != 0 && xpSlot != 5 && xpSlot != 6 && xpSlot != 7 && xpSlot != 8) {
                        l = (ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(xpSlot);
                        if (!l.isEmpty()) {
                            if (l.getItem() == Items.DIAMOND_HELMET) {
                                MendHelper.mc.playerController.windowClick(0, xpSlot, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                            }

                            MendHelper.mc.playerController.updateController();
                        }
                    }
                }

                this.delay = 0;
            }

            xpSlot = 0;

            for (i = 5; i < 9; ++i) {
                if (((ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(i)).getItem() instanceof ItemArmor) {
                    ++xpSlot;
                }
            }

            if (xpSlot == 4) {
                this.disable();
            }
        }

        if (this.clicked != 4) {
            if ((double) this.getSlotDur(5) >= this.th.getValDouble() && this.delay >= 6) {
                MendHelper.mc.playerController.windowClick(0, 5, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                ++this.clicked;
                this.delay = 0;
                MendHelper.mc.playerController.updateController();
            }

            if ((double) this.getSlotDur(6) >= this.th.getValDouble() && this.delay >= 6) {
                MendHelper.mc.playerController.windowClick(0, 6, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                ++this.clicked;
                this.delay = 0;
                MendHelper.mc.playerController.updateController();
            }

            if ((double) this.getSlotDur(7) >= this.th.getValDouble() && this.delay >= 6) {
                MendHelper.mc.playerController.windowClick(0, 7, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                ++this.clicked;
                this.delay = 0;
                MendHelper.mc.playerController.updateController();
            }

            if ((double) this.getSlotDur(8) >= this.th.getValDouble() && this.delay >= 6) {
                MendHelper.mc.playerController.windowClick(0, 8, 0, ClickType.QUICK_MOVE, MendHelper.mc.player);
                ++this.clicked;
                this.delay = 0;
                MendHelper.mc.playerController.updateController();
            }

            if (this.auto.getValBoolean() && this.delay >= 2 && this.delay != 3 && this.clicked < 4) {
                this.current = MendHelper.mc.player.inventory.currentItem;
                xpSlot = MendHelper.mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE ? MendHelper.mc.player.inventory.currentItem : -1;
                if (xpSlot == -1) {
                    for (i = 0; i < 9; ++i) {
                        if (MendHelper.mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                            xpSlot = i;
                            break;
                        }
                    }
                }

                if (xpSlot == -1 || this.current == -1) {
                    return;
                }

                MendHelper.mc.player.inventory.currentItem = xpSlot;
                MendHelper.mc.playerController.processRightClick(MendHelper.mc.player, MendHelper.mc.world, EnumHand.MAIN_HAND);
                if (this.ghost.getValBoolean()) {
                    MendHelper.mc.player.inventory.currentItem = this.current;
                }
            }

        }
    }

    public void onDisable() {
        if (MendHelper.mc.player != null) {
            this.clicked = 0;
            MendHelper.mc.player.inventory.currentItem = this.current;
        }
    }

    public int getSlotDur(int i) {
        ItemStack x = (ItemStack) MendHelper.mc.player.inventoryContainer.getInventory().get(i);
        float armorPercent = (float) (x.getMaxDamage() - x.getItemDamage()) / (float) x.getMaxDamage() * 100.0F;
        int armorBarPercent = (int) Math.min(armorPercent, 100.0F);

        return armorBarPercent;
    }
}
