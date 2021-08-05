package live.rift.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import live.rift.gui.elements.Panel;
import live.rift.module.Category;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public class ClickGui extends GuiScreen {

    public ArrayList panels = new ArrayList();
    int x = 1;
    int mx;
    int my;
    boolean addedCategories = false;

    public ClickGui() {
        Category[] acategory = Category.values();
        int i = acategory.length;

        for (int j = 0; j < i; ++j) {
            Category c = acategory[j];
            Panel cPanel = new Panel(c, this.x, 1);

            this.panels.add(cPanel);
            this.x += cPanel.cWidth + 5;
        }

    }

    public void initGui() {
        super.initGui();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        Iterator iterator = this.panels.iterator();

        while (iterator.hasNext()) {
            Panel p = (Panel) iterator.next();

            p.drawPanel(mouseX, mouseY);
        }

        this.mx = mouseX;
        this.my = mouseY;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean foundHover = false;

        Panel p;

        for (Iterator iterator = this.panels.iterator(); iterator.hasNext(); p.mouseClicked(mouseButton, mouseX, mouseY)) {
            p = (Panel) iterator.next();
            if (p.hoveringCategory(mouseX, mouseY) && !foundHover) {
                foundHover = true;
                p.categoryClick(mouseButton, mouseX, mouseY);
            }
        }

    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        Iterator iterator = this.panels.iterator();

        while (iterator.hasNext()) {
            Panel p = (Panel) iterator.next();

            p.mouseRelease(mouseX, mouseY);
        }

    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen) null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }

        Iterator iterator = this.panels.iterator();

        while (iterator.hasNext()) {
            Panel p = (Panel) iterator.next();

            p.keyTyped(typedChar, keyCode);
        }

    }

    public void handleMouseInput() throws IOException {
        Iterator iterator;
        Panel panel;

        if (Mouse.getEventDWheel() < 0) {
            iterator = this.panels.iterator();

            while (iterator.hasNext()) {
                panel = (Panel) iterator.next();
                panel.setY(panel.y + 15);
            }
        }

        if (Mouse.getEventDWheel() > 0) {
            iterator = this.panels.iterator();

            while (iterator.hasNext()) {
                panel = (Panel) iterator.next();
                panel.setY(panel.y - 15);
            }
        }

        super.handleMouseInput();
    }
}
