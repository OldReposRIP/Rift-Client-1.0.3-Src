package live.rift.gui.util;

import java.awt.Font;
import live.rift.RiftMod;
import live.rift.font.CFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiUtil {

    public static CFontRenderer cfontRenderer = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    public static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    public static String getCFont() {
        return GuiUtil.cfontRenderer.getFont().getFamily();
    }

    public FontRenderer fontRenderer() {
        return GuiUtil.fontRenderer;
    }

    public static int getStringWidth(String in) {
        return RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean() ? GuiUtil.cfontRenderer.getStringWidth(in) : GuiUtil.fontRenderer.getStringWidth(in);
    }

    public static void drawCenteredString(String text, int x, int y, int color) {
        if (RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean()) {
            GuiUtil.cfontRenderer.drawStringWithShadow(text, (double) ((float) (x - GuiUtil.fontRenderer.getStringWidth(text) / 2)), (double) ((float) y), color);
        } else {
            GuiUtil.fontRenderer.drawStringWithShadow(text, (float) (x - GuiUtil.fontRenderer.getStringWidth(text) / 2), (float) y, color);
        }

    }

    public static int space() {
        return getStringWidth(" ");
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        int f3;

        if (left < right) {
            f3 = left;
            left = right;
            right = f3;
        }

        if (top < bottom) {
            f3 = top;
            top = bottom;
            bottom = f3;
        }

        float f31 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f31);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) left, (double) bottom, 0.0D).endVertex();
        bufferbuilder.pos((double) right, (double) bottom, 0.0D).endVertex();
        bufferbuilder.pos((double) right, (double) top, 0.0D).endVertex();
        bufferbuilder.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;

            startX = endX;
            endX = i;
        }

        drawRect(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;

            startY = endY;
            endY = i;
        }

        drawRect(x, startY + 1, x + 1, endY, color);
    }

    public static void drawString(String text, int x, int y, int color) {
        if (RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean()) {
            GuiUtil.cfontRenderer.drawStringWithShadow(text, (double) x, (double) y, color);
        } else {
            GuiUtil.fontRenderer.drawStringWithShadow(text, (float) x, (float) y, color);
        }

    }

    public static void drawStringNoShadow(String text, int x, int y, int color) {
        if (RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean()) {
            GuiUtil.cfontRenderer.drawString(text, (float) x, (float) y, color);
        } else {
            GuiUtil.fontRenderer.drawString(text, x, y, color);
        }

    }

    public static int getHeight() {
        return RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean() ? GuiUtil.cfontRenderer.getHeight() : GuiUtil.fontRenderer.FONT_HEIGHT;
    }

    public static void drawString(String text, float x, float y, int color) {
        if (RiftMod.fevents.moduleManager.getModule("Hud").getSetting("CustomFont").getValBoolean()) {
            GuiUtil.cfontRenderer.drawStringWithShadow(text, (double) x, (double) y, color);
        } else {
            GuiUtil.fontRenderer.drawStringWithShadow(text, x, y, color);
        }

    }
}
