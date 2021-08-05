package live.rift.gui.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.gui.setting.eSetting;
import live.rift.gui.util.GuiUtil;
import live.rift.gui.util.PanelUtil;
import live.rift.module.Module;
import live.rift.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class Button {

    public Minecraft mc = Minecraft.getMinecraft();
    public ArrayList settings = new ArrayList();
    public Module parent;
    public Panel panel;
    public int x;
    public int y;
    public int width;
    public int height;
    public String text;
    public boolean enabled;
    public boolean visible;
    public boolean extended;
    public int titleY;
    int idleBG = (new Color(-1777332208)).getRGB();

    public Button(Panel panel, Module parent, int x, int y, int width, int height, String text) {
        this.parent = parent;
        this.panel = panel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.enabled = false;
        this.visible = true;
        this.extended = false;
        this.initSettings();
    }

    public int panelY() {
        return PanelUtil.getPanel(this.parent.getCategory().categoryName).y + this.panel.height;
    }

    public boolean isFirst() {
        return this.panel.modButtons.indexOf(this) == 0;
    }

    public boolean isLast() {
        return this.panel.modButtons.indexOf(this) == this.panel.modButtons.size() - 1;
    }

    public void drawColorModule(boolean toggled) {
        boolean toggle = toggled;

        if (this.parent.getName().equalsIgnoreCase("Gui")) {
            toggle = true;
        }

        Module guiModule = RiftMod.fevents.moduleManager.getModule("Gui");
        int guiColor;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            guiColor = Color.getHSBColor((float) (System.currentTimeMillis() % 7500L) / 7500.0F, 0.8F, 0.8F).getRGB();
        } else {
            guiColor = (new Color((int) guiModule.getSetting("Red").getValDouble(), (int) guiModule.getSetting("Green").getValDouble(), (int) guiModule.getSetting("Blue").getValDouble())).getRGB();
        }

        GuiUtil.drawRect(this.x + 2, this.y, this.x + this.panel.cWidth - 2, this.y + this.panel.height, toggle ? guiColor : -936694997);
        GuiUtil.drawString(this.text, this.x + 4, this.y + (this.panel.height - 8) / 2, -1);
    }

    public void drawButton(int mouseX, int mouseY) {
        int bgcolor = -434365412;

        if (this.visible) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int leftX = this.x;

            GuiUtil.drawRect(leftX, this.y, leftX + 2, this.y + this.panel.height, bgcolor);
            int e = this.x + this.panel.cWidth - 2;

            GuiUtil.drawRect(e, this.y, e + 2, this.y + this.panel.height, bgcolor);
            if (!this.isFirst() && !this.isLast()) {
                GuiUtil.drawRect(this.x, this.y + this.height, this.x + this.panel.cWidth, this.y + this.height + 1, bgcolor);
            }

            if (this.isFirst() && !this.isLast()) {
                GuiUtil.drawRect(this.x, this.y - 2, this.x + this.panel.cWidth, this.y, bgcolor);
                GuiUtil.drawRect(this.x, this.y + this.height, this.x + this.panel.cWidth, this.y + this.height + 1, bgcolor);
            }

            if (this.isLast() && !this.isFirst()) {
                GuiUtil.drawRect(this.x, this.y + this.height, this.x + this.panel.cWidth, this.y + this.height + 2, bgcolor);
            }

            if (this.isFirst() && this.isLast()) {
                GuiUtil.drawRect(this.x, this.y - 2, this.x + this.panel.cWidth, this.y, bgcolor);
                GuiUtil.drawRect(this.x, this.y + this.height, this.x + this.panel.cWidth, this.y + this.height + 2, bgcolor);
            }

            this.drawColorModule(this.isToggled());
        }

        if (this.extended && this.visible) {
            Iterator leftX1 = this.settings.iterator();

            while (leftX1.hasNext()) {
                eSetting e1 = (eSetting) leftX1.next();

                if (e1.visible) {
                    e1.drawSetting(mouseX, mouseY);
                }
            }
        }

    }

    public void setExtended(boolean state) {
        this.extended = state;
        eSetting e2;

        if (this.extended) {
            int bottomY = 15;

            for (Iterator e = this.settings.iterator(); e.hasNext(); bottomY += 15) {
                eSetting e1 = (eSetting) e.next();

                e1.x = this.x;
                e1.y = this.y + bottomY;
                e1.visible = true;
            }
        } else {
            for (Iterator bottomY1 = this.settings.iterator(); bottomY1.hasNext(); e2.visible = false) {
                e2 = (eSetting) bottomY1.next();
                e2.x = 0;
                e2.y = 0;
            }
        }

    }

    public int getSettingsSpace() {
        return this.settings.size() * 15;
    }

    public void initSettings() {
        if (RiftMod.setmgr.getSettingsByMod(this.parent) != null) {
            Iterator iterator = RiftMod.setmgr.getSettingsByMod(this.parent).iterator();

            while (iterator.hasNext()) {
                Setting s = (Setting) iterator.next();

                this.settings.add(new eSetting(this, s, 0, 0, 100, 15));
            }
        }

    }

    public boolean isToggled() {
        return this.parent.getState();
    }

    public void mouseClicked(int mButton, int mouseX, int mouseY) {
        if (this.isHovering(mouseX, mouseY) && mButton == 0) {
            this.parent.setState(!this.parent.getState());
        }

    }

    public boolean isHovering(int mouseX, int mouseY) {
        return !this.visible ? false : mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }
}
