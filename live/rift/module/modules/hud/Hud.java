package live.rift.module.modules.hud;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import live.rift.DiscordPresence;
import live.rift.RiftMod;
import live.rift.command.commands.Status;
import live.rift.font.CFontRenderer;
import live.rift.gui.util.GuiUtil;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.RainbowUtil;
import live.rift.util.UIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class Hud extends Module {

    public Setting watermark = new Setting("Watermark", this, true);
    public Setting cfont = new Setting("CustomFont", this, true);
    public Setting modulearray = new Setting("ArrayList", this, true);
    public Setting arraypos;
    public ArrayList arrayPositions = new ArrayList();
    public Setting rainbowspeed;
    public Setting colordiff;
    public Setting offsety;
    public Setting offsetx;
    public Setting armorhud;
    public Setting welcomer = new Setting("Welcomer", this, true);
    public Setting playerView;
    public Setting pvpInfo;
    public Setting itemCount;
    public Setting durabilityWarning;
    public Setting durabilityThreshold;
    public Setting custom_fov;
    public Setting custom_fov_bool;
    public static float old_fov = Hud.mc.gameSettings.fovSetting;
    public static CFontRenderer waterRenderer = new CFontRenderer(new Font("Arial", 0, 26), true, false);
    public static CFontRenderer waterRenderer2 = new CFontRenderer(new Font("Arial", 0, 22), true, false);
    public static CFontRenderer armorRenderer = new CFontRenderer(new Font("Arial", 0, 18), true, false);
    ArrayList enabled = new ArrayList();
    RainbowUtil rutil = new RainbowUtil(9);

    public Hud() {
        super("Hud", 0, Category.HUD);
        this.arrayPositions.add("TopLeft");
        this.arrayPositions.add("TopRight");
        this.arrayPositions.add("BottomRight");
        this.arrayPositions.add("BottomLeft");
        this.arraypos = new Setting("A Pos", this, "TopLeft", this.arrayPositions);
        this.rainbowspeed = new Setting("A RainbowSpeed", this, 20.0D, 1.0D, 50.0D, true);
        this.colordiff = new Setting("A ColorDiff", this, 10.0D, 1.0D, 40.0D, true);
        this.offsety = new Setting("A Offset Y", this, 16.0D, 0.0D, 500.0D, true);
        this.offsetx = new Setting("A Offset X", this, 0.0D, 0.0D, 500.0D, true);
        this.armorhud = new Setting("ArmorHUD", this, true);
        this.pvpInfo = new Setting("PVP Info", this, false);
        this.playerView = new Setting("Player Model", this, false);
        this.itemCount = new Setting("PVP Item Count", this, false);
        this.durabilityWarning = new Setting("Low Armor Warning", this, false);
        this.durabilityThreshold = new Setting("Armor Warning Threshold", this, 0.0D, 0.0D, 100.0D, true);
        this.custom_fov_bool = new Setting("Custom FOV", this, false);
        this.custom_fov = new Setting("FOV", this, 100.0D, 10.0D, 200.0D, true);
    }

    public void onEnable() {
        Iterator iterator = RiftMod.fevents.moduleManager.modules.iterator();

        while (iterator.hasNext()) {
            Module mod = (Module) iterator.next();

            this.initList(mod);
        }

    }

    public void initList(Module mod) {
        Comparator comparator;

        if (this.enabled.contains(mod) && (mod.isDisabled() || !mod.isVisible())) {
            this.enabled.remove(mod);
            comparator = (first, second) -> {
                String firstName = first.getName() + first.getModInfo();
                String secondName = second.getName() + second.getModInfo();
                float dif = (float) (GuiUtil.getStringWidth(secondName) - GuiUtil.getStringWidth(firstName));

                return dif != 0.0F ? (int) dif : secondName.compareTo(firstName);
            };
            this.enabled.sort(comparator);
        }

        if (!this.enabled.contains(mod) && mod.isEnabled() && !mod.getCategory().equals(Category.HUD) && mod.isVisible()) {
            this.enabled.add(mod);
            comparator = (first, second) -> {
                String firstName = first.getName() + first.getModInfo();
                String secondName = second.getName() + second.getModInfo();
                float dif = (float) (GuiUtil.getStringWidth(secondName) - GuiUtil.getStringWidth(firstName));

                return dif != 0.0F ? (int) dif : secondName.compareTo(firstName);
            };
            this.enabled.sort(comparator);
        }

    }

    public void onDisable() {
        Hud.mc.gameSettings.fovSetting = Hud.old_fov;
        Iterator iterator = RiftMod.fevents.moduleManager.modules.iterator();

        while (iterator.hasNext()) {
            Module mod = (Module) iterator.next();

            this.initList(mod);
        }

    }

    public int getColor(int index) {
        boolean color = true;
        int color1;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            color1 = this.rutil.GetRainbowColorAt(index);
        } else {
            color1 = (new Color((int) RiftMod.setmgr.getSettingByNameMod("Red", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Green", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Blue", "Gui").getValDouble())).getRGB();
        }

        return color1;
    }

    public void onUpdate() {
        if (Minecraft.getMinecraft().player != null) {
            DiscordPresence.presence.smallImageKey = Hud.mc.player.getName().toLowerCase();
            DiscordPresence.presence.smallImageText = Hud.mc.player.getName();
        }

        if (Status.statusParsed != null) {
            DiscordPresence.presence.state = Status.statusParsed;
            DiscordPresence.rpc.Discord_UpdatePresence(DiscordPresence.presence);
        }

        if ((double) this.rutil.getTimer() != this.rainbowspeed.getMax() - this.rainbowspeed.getValDouble()) {
            this.rutil.SetTimer((int) (this.rainbowspeed.getMax() - this.rainbowspeed.getValDouble()));
        }

        if (this.custom_fov_bool.getValBoolean()) {
            Hud.mc.gameSettings.fovSetting = this.custom_fov.getValFloat();
        }

    }

    public void onRender() {
        ScaledResolution scr = new ScaledResolution(Hud.mc);
        int ri = 0;

        this.rutil.onRender();
        int armorPercent;
        int i;
        int j;
        int k;

        if (this.modulearray.getValBoolean()) {
            Iterator shouldWarn = RiftMod.fevents.moduleManager.modules.iterator();

            while (shouldWarn.hasNext()) {
                Module items = (Module) shouldWarn.next();

                this.initList(items);
            }

            Module x;
            Iterator iterator;

            if (this.arraypos.getValString().equalsIgnoreCase("TopLeft")) {
                i = (int) (1.0D + this.offsety.getValDouble());

                for (iterator = this.enabled.iterator(); iterator.hasNext(); i += 10) {
                    x = (Module) iterator.next();
                    ri = (int) ((double) ri + this.colordiff.getValDouble());
                    if (ri >= 355) {
                        ri = 0;
                    }

                    GuiUtil.drawString(x.getName(), 1, i, this.getColor(ri));
                    GuiUtil.drawString(x.getModInfo(), 1 + GuiUtil.space() + GuiUtil.getStringWidth(x.getName()), i, -8224126);
                }
            } else if (this.arraypos.getValString().equalsIgnoreCase("TopRight")) {
                i = (int) (1.0D + this.offsety.getValDouble());

                for (iterator = this.enabled.iterator(); iterator.hasNext(); i += 10) {
                    x = (Module) iterator.next();
                    ri = (int) ((double) ri + this.colordiff.getValDouble());
                    if (ri >= 355) {
                        ri = 0;
                    }

                    armorPercent = (int) ((double) (UIUtil.rightSide() - (GuiUtil.getStringWidth(x.getName()) + GuiUtil.getStringWidth(x.getModInfo())) - 1) + this.offsetx.getValDouble());
                    if (!x.getModInfo().equals("")) {
                        armorPercent -= GuiUtil.space();
                    }

                    GuiUtil.drawString(x.getName(), armorPercent, i, this.getColor(ri));
                    GuiUtil.drawString(x.getModInfo(), armorPercent + GuiUtil.space() + GuiUtil.getStringWidth(x.getName()), i, -8224126);
                }
            } else if (this.arraypos.getValString().equalsIgnoreCase("BottomRight")) {
                i = (int) ((double) (UIUtil.bottomY() - this.enabled.size() * GuiUtil.getHeight() - GuiUtil.getHeight() - 1) - this.offsety.getValDouble());
                if (Hud.mc.currentScreen instanceof GuiChat) {
                    i = i - GuiUtil.getHeight() - GuiUtil.getHeight() / 2;
                }

                for (j = this.enabled.size() - 1; j >= 0; --j) {
                    ri = (int) ((double) ri + this.colordiff.getValDouble());
                    if (ri >= 355) {
                        ri = 0;
                    }

                    k = (int) ((double) (UIUtil.rightSide() - (GuiUtil.getStringWidth(((Module) this.enabled.get(j)).getName()) + GuiUtil.getStringWidth(((Module) this.enabled.get(j)).getModInfo())) - 1) + this.offsetx.getValDouble());
                    if (!((Module) this.enabled.get(j)).getModInfo().equals("")) {
                        k -= GuiUtil.space();
                    }

                    GuiUtil.drawString(((Module) this.enabled.get(j)).getName(), k, i, this.getColor(ri));
                    GuiUtil.drawString(((Module) this.enabled.get(j)).getModInfo(), k + GuiUtil.space() + GuiUtil.getStringWidth(((Module) this.enabled.get(j)).getName()), i, -8224126);
                    i += 10;
                }
            } else if (this.arraypos.getValString().equalsIgnoreCase("BottomLeft")) {
                i = (int) ((double) (UIUtil.bottomY() - this.enabled.size() * GuiUtil.getHeight() - GuiUtil.getHeight() - 1) - this.offsety.getValDouble());
                if (Hud.mc.currentScreen instanceof GuiChat) {
                    i = i - GuiUtil.getHeight() - GuiUtil.getHeight() / 2;
                }

                for (j = this.enabled.size() - 1; j >= 0; --j) {
                    ri = (int) ((double) ri + this.colordiff.getValDouble());
                    if (ri >= 355) {
                        ri = 0;
                    }

                    GuiUtil.drawString(((Module) this.enabled.get(j)).getName(), 1, i, this.getColor(ri));
                    GuiUtil.drawString(((Module) this.enabled.get(j)).getModInfo(), 1 + GuiUtil.space() + GuiUtil.getStringWidth(((Module) this.enabled.get(j)).getName()), i, -8224126);
                    i += 10;
                }
            }
        }

        if (this.watermark.getValBoolean()) {
            ri = (int) ((double) ri + this.colordiff.getValDouble());
            if (ri >= 355) {
                ri = 0;
            }

            if (this.cfont.getValBoolean()) {
                Hud.waterRenderer.drawStringWithShadow("Rift", 1.0D, 1.0D, this.getColor(ri));
                Hud.waterRenderer2.drawStringWithShadow("b1.0.3", (double) (4 + Hud.waterRenderer.getStringWidth("Rift")), 3.0D, this.getColor(ri));
            } else {
                Hud.mc.fontRenderer.drawStringWithShadow("Rift b1.0.3", 1.0F, 1.0F, this.getColor(ri));
            }
        }

        int armorBarPercent;
        int y;
        int x1;
        NonNullList nonnulllist;

        if (this.armorhud.getValBoolean()) {
            i = scr.getScaledWidth() / 2;
            GlStateManager.enableAlpha();
            nonnulllist = Hud.mc.player.inventory.armorInventory;
            GlStateManager.clear(256);

            for (k = nonnulllist.size() - 1; k >= 0; --k) {
                armorPercent = i - 90 + 100 + 6;
                armorBarPercent = scr.getScaledHeight() - 16 - 15 - 8;
                y = armorPercent + 1 + nonnulllist.size() - 1 - k % 9 * 18;
                int i = armorBarPercent + 1 + (k / 9 - 1) * 18;

                GlStateManager.pushMatrix();
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                RenderHelper.enableGUIStandardItemLighting();
                Hud.mc.getRenderItem().renderItemAndEffectIntoGUI((ItemStack) nonnulllist.get(k), y + 50, i);
                Hud.mc.getRenderItem().renderItemOverlays(Hud.mc.fontRenderer, (ItemStack) nonnulllist.get(k), y + 50, i);
                float s = (float) (((ItemStack) nonnulllist.get(k)).getMaxDamage() - ((ItemStack) nonnulllist.get(k)).getItemDamage()) / (float) ((ItemStack) nonnulllist.get(k)).getMaxDamage() * 100.0F;

                x1 = (int) Math.min(s, 100.0F);
                Hud.armorRenderer.drawString(String.valueOf(x1), (float) (y + 53), (float) (i - 10), ((ItemStack) nonnulllist.get(k)).getItem().getRGBDurabilityForDisplay((ItemStack) nonnulllist.get(k)));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.popMatrix();
            }
        }

        if (this.welcomer.getValBoolean() && this.cfont.getValBoolean()) {
            Hud.armorRenderer.drawStringWithShadow("Welcome to Rift, " + (Hud.mc.player == null ? "NULL" : Hud.mc.player.getName() + " :^)"), (double) (scr.getScaledWidth() / 2 - Hud.armorRenderer.getStringWidth("Welcome to Rift, " + (Hud.mc.player == null ? "NULL" : Hud.mc.player.getName() + " :^)")) / 2), 8.0D, this.getColor(ri));
        } else if (this.welcomer.getValBoolean() && !this.cfont.getValBoolean()) {
            Hud.mc.fontRenderer.drawStringWithShadow("Welcome to Rift, " + (Hud.mc.player == null ? "NULL" : Hud.mc.player.getName() + " :^)"), (float) (scr.getScaledWidth() / 2 - Hud.mc.fontRenderer.getStringWidth("Welcome to Rift, " + (Hud.mc.player == null ? "NULL" : Hud.mc.player.getName() + " :^)")) / 2), 8.0F, this.getColor(ri));
        }

        if (this.playerView.getValBoolean() && Hud.mc.player != null) {
            float f = Hud.mc.getRenderViewEntity().rotationYaw * -1.0F;
            float f1 = Hud.mc.getRenderViewEntity().rotationPitch * -1.0F;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            k = this.arraypos.getValString().endsWith("Left") ? 25 : UIUtil.rightSide() - 25;
            armorPercent = this.arraypos.getValString().startsWith("Bottom") ? 62 + (int) this.offsety.getValDouble() : 10 * this.enabled.size() + 62 + (int) this.offsety.getValDouble();
            drawEntityOnScreen(k, armorPercent, 30, f, f1, Hud.mc.player);
        }

        if (this.pvpInfo.getValBoolean()) {
            String s = "CA: " + (RiftMod.fevents.moduleManager.getModule("AutoCrystal").isEnabled() ? "ON" : "OFF");
            String s1 = "AT: " + (RiftMod.fevents.moduleManager.getModule("AutoTrap").isEnabled() ? "ON" : "OFF");
            String s2 = "HF: " + (RiftMod.fevents.moduleManager.getModule("HoleFiller").isEnabled() ? "ON" : "OFF");
            String s3 = "SU: " + (RiftMod.fevents.moduleManager.getModule("Surround").isEnabled() ? "ON" : "OFF");
            ArrayList arraylist = new ArrayList();

            arraylist.add(s);
            arraylist.add(s1);
            arraylist.add(s3);
            arraylist.add(s2);
            y = this.arraypos.getValString().startsWith("Bottom") ? 6 + (int) this.offsety.getValDouble() : 10 * this.enabled.size() + 6 + (int) this.offsety.getValDouble();

            String s4;

            for (Iterator iterator1 = arraylist.iterator(); iterator1.hasNext(); y += (this.cfont.getValBoolean() ? Hud.armorRenderer.getStringHeight(s4) : Hud.mc.fontRenderer.FONT_HEIGHT) + 2) {
                s4 = (String) iterator1.next();
                x1 = this.arraypos.getValString().endsWith("Left") ? (this.playerView.getValBoolean() ? 50 : 1) : UIUtil.rightSide() - (this.playerView.getValBoolean() ? 50 : 1) - (this.cfont.getValBoolean() ? Hud.armorRenderer.getStringWidth(s4) : Hud.mc.fontRenderer.getStringWidth(s4));
                if (this.cfont.getValBoolean()) {
                    Hud.armorRenderer.drawStringWithShadow(s4, (double) x1, (double) y, this.getColor(ri));
                } else {
                    Hud.mc.fontRenderer.drawStringWithShadow(s4, (float) x1, (float) y, this.getColor(ri));
                }
            }
        }

        if (this.itemCount.getValBoolean()) {
            i = !this.playerView.getValBoolean() && !this.pvpInfo.getValBoolean() ? 80 : (this.playerView.getValBoolean() ? 45 : 20) + (this.pvpInfo.getValBoolean() ? 45 : 20);
            j = !this.playerView.getValBoolean() && !this.pvpInfo.getValBoolean() ? (this.arraypos.getValString().startsWith("Bottom") ? 6 + (int) this.offsety.getValDouble() : 10 * this.enabled.size() + 6 + (int) this.offsety.getValDouble()) : (this.arraypos.getValString().startsWith("Bottom") ? 6 + (int) this.offsety.getValDouble() : 10 * this.enabled.size() + 6 + (int) this.offsety.getValDouble()) + 62;
            k = this.arraypos.getValString().endsWith("Left") ? 2 : UIUtil.rightSide() - i - 2;
            ArrayList arraylist1 = new ArrayList();

            arraylist1.add(this.renderStack(Items.END_CRYSTAL));
            arraylist1.add(this.renderStack(Items.GOLDEN_APPLE));
            arraylist1.add(this.renderStack(Items.EXPERIENCE_BOTTLE));
            arraylist1.add(this.renderStack(Items.TOTEM_OF_UNDYING));
            armorBarPercent = i / 4;

            for (Iterator iterator2 = arraylist1.iterator(); iterator2.hasNext(); k += armorBarPercent) {
                ItemStack itemstack = (ItemStack) iterator2.next();

                GlStateManager.pushMatrix();
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                RenderHelper.enableGUIStandardItemLighting();
                Hud.mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, k, j);
                Hud.mc.getRenderItem().renderItemOverlays(Hud.mc.fontRenderer, itemstack, k, j);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.popMatrix();
            }
        }

        if (this.durabilityWarning.getValBoolean()) {
            boolean flag = false;

            nonnulllist = Hud.mc.player.inventory.armorInventory;

            for (k = nonnulllist.size() - 1; k >= 0; --k) {
                float f2 = (float) (((ItemStack) nonnulllist.get(k)).getMaxDamage() - ((ItemStack) nonnulllist.get(k)).getItemDamage()) / (float) ((ItemStack) nonnulllist.get(k)).getMaxDamage() * 100.0F;

                armorBarPercent = (int) Math.min(f2, 100.0F);
                if ((double) f2 <= this.durabilityThreshold.getValDouble()) {
                    flag = true;
                }
            }

            if (flag) {
                ;
            }
        }

    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableColorMaterial();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();

        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
    }

    public ItemStack renderStack(Item i) {
        ItemStack stack = new ItemStack(i);

        stack.stackSize = getItems(i);
        if (i == Items.GOLDEN_APPLE) {
            stack.itemDamage = 1;
        }

        return stack;
    }

    public static int getItems(Item i) {
        return Hud.mc.player.inventory.mainInventory.stream().filter((itemStack) -> {
            return itemStack.getItem() == i;
        }).mapToInt(ItemStack::getCount).sum() + Hud.mc.player.inventory.offHandInventory.stream().filter((itemStack) -> {
            return itemStack.getItem() == i;
        }).mapToInt(ItemStack::getCount).sum();
    }
}
