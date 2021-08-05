package live.rift.gui.setting;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import live.rift.RiftMod;
import live.rift.gui.elements.Button;
import live.rift.gui.util.GuiUtil;
import live.rift.module.Module;
import live.rift.setting.Setting;
import org.lwjgl.input.Keyboard;

public class eSetting {

    public int x;
    public int y;
    public int width;
    public int height;
    public Setting setting;
    public boolean visible;
    public boolean dragging;
    public boolean listening;
    public Button parent;

    public eSetting(Button parent, Setting setting, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.setting = setting;
        this.visible = false;
        this.dragging = false;
        this.listening = false;
        this.parent = parent;
    }

    public void drawSetting(int mouseX, int mouseY) {
        Module guiModule = RiftMod.fevents.moduleManager.getModule("Gui");
        int guiColor;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            guiColor = Color.getHSBColor((float) (System.currentTimeMillis() % 7500L) / 7500.0F, 0.8F, 0.8F).getRGB();
        } else {
            guiColor = (new Color((int) guiModule.getSetting("Red").getValDouble(), (int) guiModule.getSetting("Green").getValDouble(), (int) guiModule.getSetting("Blue").getValDouble())).getRGB();
        }

        int hoverColor = (new Color(137, 137, 137, 200)).getRGB();

        if (this.visible) {
            double percent;

            if (this.dragging) {
                percent = (double) Math.min(100, Math.max(0, mouseX - this.x));
                BigDecimal off = new BigDecimal(percent / 100.0D * (this.setting.getMax() - this.setting.getMin()) + this.setting.getMin());

                off = off.setScale(2, RoundingMode.HALF_UP);
                double sliderX = off.doubleValue();

                this.setting.setValDouble(sliderX);
            }

            if (this.setting.isBind()) {
                GuiUtil.drawRect(this.x, ((Setting) RiftMod.setmgr.getSettingsByMod(this.setting.getParentMod()).get(0)).equals(this.setting) ? this.y + 1 : this.y, this.x + this.parent.panel.cWidth, this.y + this.parent.panel.height + 1, -434365412);
                if (!this.listening) {
                    GuiUtil.drawString("Keybind", this.x + 6, this.y + (this.parent.panel.height - 8) / 2, -1);
                    GuiUtil.drawString(this.setting.getKeyBind() > -1 ? Keyboard.getKeyName(this.setting.getKeyBind()) : "", this.x + 6 + GuiUtil.getStringWidth("Keybind") + this.space(), this.y + (this.parent.panel.height - 8) / 2, -7566196);
                } else {
                    GuiUtil.drawRect(this.x + 3, this.y + 1, this.x + this.parent.panel.cWidth - 3, this.y + this.parent.panel.height, guiColor);
                    GuiUtil.drawString("Press a Key...", this.x + 6, this.y + (this.parent.panel.height - 8) / 2, -1);
                }
            }

            if (this.setting.isCheck()) {
                if (((Setting) RiftMod.setmgr.getSettingsByMod(this.setting.getParentMod()).get(0)).equals(this.setting)) {
                    GuiUtil.drawHorizontalLine(this.x, this.x + this.parent.panel.cWidth - 1, this.y - 1, -434365412);
                }

                GuiUtil.drawRect(this.x, ((Setting) RiftMod.setmgr.getSettingsByMod(this.setting.getParentMod()).get(0)).equals(this.setting) ? this.y + 1 : this.y, this.x + this.parent.panel.cWidth, this.y + this.parent.panel.height + 1, -434365412);
                if (this.setting.getValBoolean()) {
                    GuiUtil.drawRect(this.x + 3, this.y, this.x + this.parent.panel.cWidth - 3, this.y + this.parent.panel.height, guiColor);
                } else if (!this.setting.getValBoolean() && this.isHovering(mouseX, mouseY)) {
                    GuiUtil.drawRect(this.x + 3, this.y, this.x + this.parent.panel.cWidth - 3, this.y + this.parent.panel.height, hoverColor);
                }

                GuiUtil.drawString(this.setting.getName(), this.x + 6, this.y + (this.parent.panel.height - 1 - 8) / 2, -1);
            }

            if (this.setting.isSlider()) {
                GuiUtil.drawRect(this.x, ((Setting) RiftMod.setmgr.getSettingsByMod(this.setting.getParentMod()).get(0)).equals(this.setting) ? this.y + 1 : this.y, this.x + this.parent.panel.cWidth, this.y + this.parent.panel.height + 1, -434365412);
                percent = this.setting.getValDouble() / this.setting.getMax();
                int off1 = this.width - 3;
                int sliderX1 = (int) ((double) off1 * percent);

                if ((this.setting.getValDouble() < 0.0D || this.setting.getValDouble() > 0.1D) && this.x + sliderX1 - 3 >= this.x) {
                    GuiUtil.drawRect(this.x + 3, this.y, this.x + sliderX1, this.y + this.parent.panel.height, guiColor);
                }

                GuiUtil.drawString(this.setting.getName(), this.x + 6, this.y + (this.parent.panel.height - 8) / 2, -1);
                GuiUtil.drawString(Double.toString(this.setting.getValDouble()), this.x + 6 + GuiUtil.getStringWidth(this.setting.getName()) + this.space(), this.y + (this.parent.panel.height - 8) / 2, -7566196);
            }

            if (this.setting.isCombo()) {
                GuiUtil.drawRect(this.x, ((Setting) RiftMod.setmgr.getSettingsByMod(this.setting.getParentMod()).get(0)).equals(this.setting) ? this.y + 1 : this.y, this.x + this.parent.panel.cWidth, this.y + this.parent.panel.height + 1, -434365412);
                GuiUtil.drawRect(this.x + 3, this.y, this.x + this.parent.panel.cWidth - 3, this.y + this.parent.panel.height, guiColor);
                GuiUtil.drawString(this.setting.getName(), this.x + 6, this.y + (this.parent.panel.height - 8) / 2, -1);
                GuiUtil.drawString(this.setting.getValString(), this.x + 6 + GuiUtil.getStringWidth(this.setting.getName()) + this.space(), this.y + (this.parent.panel.height - 8) / 2, -7566196);
            }
        }

    }

    public int space() {
        return GuiUtil.getStringWidth(" ");
    }

    public int getBGSize() {
        return this.parent.getSettingsSpace();
    }

    public void mouseClicked(int mouseX, int mouseY, int mButton) {
        if (this.visible && this.isHovering(mouseX, mouseY) && mButton == 0) {
            if (this.setting.isCheck()) {
                this.setting.setValBoolean(!this.setting.getValBoolean());
            }

            if (this.setting.isSlider()) {
                this.dragging = true;
            }

            if (this.setting.isBind() && !this.listening) {
                this.listening = true;
            }

            if (this.setting.isCombo()) {
                if (this.setting.getOptions().indexOf(this.setting.getValString()) == this.setting.getOptions().size() - 1) {
                    this.setting.setValString((String) this.setting.getOptions().get(0));
                } else {
                    this.setting.setValString((String) this.setting.getOptions().get(this.setting.getOptions().indexOf(this.setting.getValString()) + 1));
                }
            }
        }

    }

    public void mouseRelease(int mouseX, int mouseY) {
        if (this.setting.isSlider()) {
            this.dragging = false;
        }

    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listening) {
            if (keyCode == 42) {
                this.setting.getParentMod().setKey(0);
                this.listening = false;
            } else if (keyCode == 1) {
                this.listening = false;
            } else {
                this.setting.getParentMod().setKey(keyCode);
                this.listening = false;
            }
        }

    }
}
