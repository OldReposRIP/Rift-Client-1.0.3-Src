package live.rift.util;

import net.minecraft.client.Minecraft;

public class UIUtil {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static int x_position = 0;
    public static int y_position = 0;
    public static int gitIsShitAsFuck = 0;

    public static int scaleX(UIUtil.ScreenPos screenpos, int width, int offset_x, int offset_y, int scale) {
        int xpos = 0;

        switch (screenpos) {
        case TOP_LEFT:
            xpos = (0 + offset_x) / scale;
            break;

        case TOP_RIGHT:
            xpos = (UIUtil.mc.displayWidth / 2 - width * scale - offset_x) / scale;
            break;

        case BOTTOM_LEFT:
            xpos = (0 + offset_x) / scale;
            break;

        case BOTTOM_RIGHT:
            xpos = (UIUtil.mc.displayWidth / 2 - width * scale - offset_x) / scale;
        }

        return xpos;
    }

    public static int scaleY(UIUtil.ScreenPos screenpos, int height, int offset_x, int offset_y, int scale) {
        int ypos = 0;

        switch (screenpos) {
        case TOP_LEFT:
            ypos = (0 + offset_y) / scale;
            break;

        case TOP_RIGHT:
            ypos = (0 + offset_y) / scale;
            break;

        case BOTTOM_LEFT:
            ypos = (UIUtil.mc.displayHeight / 2 - height * scale - offset_y) / scale;
            break;

        case BOTTOM_RIGHT:
            ypos = (UIUtil.mc.displayHeight / 2 - height * scale - offset_y) / scale;
        }

        return ypos;
    }

    public static void positionText(UIUtil.ScreenPos position_on_screen, String text, float offset_x, float offset_y, float scale) {
        switch (position_on_screen) {
        case TOP_LEFT:
            UIUtil.x_position = (int) ((0.0F + offset_x) / scale);
            UIUtil.y_position = (int) ((0.0F + offset_y) / scale);
            break;

        case TOP_RIGHT:
            UIUtil.x_position = (int) (((float) (UIUtil.mc.displayWidth / 2) - (float) UIUtil.mc.fontRenderer.getStringWidth(text) * scale - offset_x) / scale);
            UIUtil.y_position = (int) ((0.0F + offset_y) / scale);
            break;

        case BOTTOM_LEFT:
            UIUtil.x_position = (int) ((0.0F + offset_x) / scale);
            UIUtil.y_position = (int) (((float) (UIUtil.mc.displayHeight / 2) - (float) UIUtil.mc.fontRenderer.FONT_HEIGHT * scale - offset_y) / scale);
            break;

        case BOTTOM_RIGHT:
            UIUtil.x_position = (int) (((float) (UIUtil.mc.displayWidth / 2) - (float) UIUtil.mc.fontRenderer.getStringWidth(text) * scale - offset_x) / scale);
            UIUtil.y_position = (int) (((float) (UIUtil.mc.displayHeight / 2) - (float) UIUtil.mc.fontRenderer.FONT_HEIGHT * scale - offset_y) / scale);
        }

    }

    public static int rightSide() {
        return (UIUtil.mc.displayWidth / 2 - 0) / 1;
    }

    public static int bottomY() {
        return (UIUtil.mc.displayHeight / 2 - 0) / 1;
    }

    public static enum ScreenPos {

        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT;
    }
}
