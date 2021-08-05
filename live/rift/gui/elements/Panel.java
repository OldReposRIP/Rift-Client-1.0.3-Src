package live.rift.gui.elements;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.gui.setting.eSetting;
import live.rift.gui.util.GuiUtil;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.util.UIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class Panel {

    public ArrayList modButtons = new ArrayList();
    public Category category;
    public int x;
    public int y;
    public boolean dragging;
    public boolean extended;
    public int height = 15;
    public int bottomY;
    public int cWidth;

    public Panel(Category category, int x, int y) {
        this.bottomY = this.height + 2;
        this.cWidth = 100;
        this.category = category;
        this.x = x;
        this.y = y;
        this.dragging = false;
        this.extended = true;
        this.addModules();
    }

    public void drawPanel(int mouseX, int mouseY) {
        Module guiModule = RiftMod.fevents.moduleManager.getModule("Gui");
        int guiColor;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            guiColor = Color.getHSBColor((float) (System.currentTimeMillis() % 7500L) / 7500.0F, 0.8F, 0.8F).getRGB();
        } else {
            guiColor = (new Color((int) guiModule.getSetting("Red").getValDouble(), (int) guiModule.getSetting("Green").getValDouble(), (int) guiModule.getSetting("Blue").getValDouble())).getRGB();
        }

        this.categoryDraw(guiColor);
        Iterator iterator = this.modButtons.iterator();

        while (iterator.hasNext()) {
            Button e = (Button) iterator.next();

            e.drawButton(mouseX, mouseY);
        }

    }

    public void categoryDraw(int color) {
        GuiUtil.drawRect(this.x, this.y, this.x + this.cWidth, this.y + this.height, color);
        GuiUtil.drawString(this.category.categoryName, this.x + 3, this.y + (this.height - 8) / 2, -1);
        if (this.extended) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            UIUtil.positionText(UIUtil.ScreenPos.TOP_LEFT, "˅", (float) (this.x + 85), (float) (this.y + (this.height - 8) / 2), 2.0F);
            float f = (float) UIUtil.x_position;
            float f1 = (float) UIUtil.y_position;

            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("˅", f, f1, -1);
            GlStateManager.popMatrix();
        } else {
            GuiUtil.drawString(">", this.x + 90, this.y + (this.height - 8) / 2, -1);
        }

    }

    public void addModules() {
        Iterator iterator = RiftMod.fevents.moduleManager.modules.iterator();

        while (iterator.hasNext()) {
            Module module = (Module) iterator.next();

            if (module.getCategory().equals(this.category)) {
                this.modButtons.add(new Button(this, module, this.x, this.y + this.bottomY, this.cWidth, this.height, module.name));
                this.bottomY += this.height + 1;
            }
        }

    }

    public void toggleExtend() {
        Button b;

        for (Iterator iterator = this.modButtons.iterator(); iterator.hasNext(); b.visible = !b.visible) {
            b = (Button) iterator.next();
            if (b.visible) {
                this.extended = false;
            } else {
                this.extended = true;
            }
        }

    }

    public boolean hoveringCategory(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.cWidth && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public void categoryClick(int mButton, int mouseX, int mouseY) {
        if (this.hoveringCategory(mouseX, mouseY) && mButton == 1) {
            this.toggleExtend();
        }

        if (this.hoveringCategory(mouseX, mouseY) && mButton == 0) {
            this.dragging = true;
        }

    }

    public void mouseClicked(int mButton, int mouseX, int mouseY) {
        Iterator iterator = this.modButtons.iterator();

        Button b;
        Iterator iterator1;

        while (iterator.hasNext()) {
            b = (Button) iterator.next();
            if (b.isHovering(mouseX, mouseY) && mButton == 1) {
                iterator1 = this.modButtons.iterator();

                while (iterator1.hasNext()) {
                    Button s = (Button) iterator1.next();
                    Iterator iterator2;
                    eSetting s1;

                    if (b.extended) {
                        if (s.y + s.height > b.y + b.height) {
                            s.y -= b.getSettingsSpace();
                        }

                        iterator2 = s.settings.iterator();

                        while (iterator2.hasNext()) {
                            s1 = (eSetting) iterator2.next();
                            if (s1.y + s1.height > b.y + b.height) {
                                s1.y -= b.getSettingsSpace();
                            }
                        }
                    } else {
                        if (s.y + s.height > b.y + b.height) {
                            s.y += b.getSettingsSpace();
                        }

                        iterator2 = s.settings.iterator();

                        while (iterator2.hasNext()) {
                            s1 = (eSetting) iterator2.next();
                            if (s1.y + s1.height > b.y + b.height) {
                                s1.y += b.getSettingsSpace();
                            }
                        }
                    }
                }

                b.setExtended(!b.extended);
            }
        }

        iterator = this.modButtons.iterator();

        while (iterator.hasNext()) {
            b = (Button) iterator.next();
            if (b.isHovering(mouseX, mouseY) && mButton != 1) {
                b.mouseClicked(mButton, mouseX, mouseY);
            }

            iterator1 = b.settings.iterator();

            while (iterator1.hasNext()) {
                eSetting s2 = (eSetting) iterator1.next();

                s2.mouseClicked(mouseX, mouseY, mButton);
            }
        }

    }

    public void mouseRelease(int mouseX, int mouseY) {
        if (this.dragging) {
            this.dragging = false;
        }

        Iterator oldy = this.modButtons.iterator();

        while (oldy.hasNext()) {
            Button b = (Button) oldy.next();
            Iterator e = b.settings.iterator();

            while (e.hasNext()) {
                eSetting s = (eSetting) e.next();

                s.mouseRelease(mouseX, mouseY);
            }
        }

        int oldy1 = this.y;
        Iterator b1 = this.modButtons.iterator();

        while (b1.hasNext()) {
            Button e1 = (Button) b1.next();

            e1.x = this.x;
            e1.y += this.y - oldy1;

            eSetting es;

            for (Iterator s1 = e1.settings.iterator(); s1.hasNext(); es.y += this.y - oldy1) {
                es = (eSetting) s1.next();
                es.x = this.x;
            }
        }

    }

    public void setY(int y) {
        int oldy = this.y;

        this.y = y;
        Iterator iterator = this.modButtons.iterator();

        while (iterator.hasNext()) {
            Button e = (Button) iterator.next();

            e.y += y - oldy;

            eSetting es;

            for (Iterator iterator1 = e.settings.iterator(); iterator1.hasNext(); es.y += y - oldy) {
                es = (eSetting) iterator1.next();
            }
        }

    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        Iterator iterator = this.modButtons.iterator();

        while (iterator.hasNext()) {
            Button b = (Button) iterator.next();
            Iterator iterator1 = b.settings.iterator();

            while (iterator1.hasNext()) {
                eSetting s = (eSetting) iterator1.next();

                s.keyTyped(typedChar, keyCode);
            }
        }

    }
}
