package live.rift.util;

import java.awt.Rectangle;
import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderingMethods {

    private static float FADING_HUE = 1.0F;

    public static float getFadingHue() {
        return RenderingMethods.FADING_HUE;
    }

    public static void drawBorderedRect(int x, int y, int x1, int y1, float width, int internalColor, int borderColor) {
        enableGL2D();
        Gui.drawRect(x + (int) width, y + (int) width, x1 - (int) width, y1 - (int) width, internalColor);
        Gui.drawRect(x + (int) width, y, x1 - (int) width, y + (int) width, borderColor);
        Gui.drawRect(x, y, x + (int) width, y1, borderColor);
        Gui.drawRect(x1 - (int) width, y, x1, y1, borderColor);
        Gui.drawRect(x + (int) width, y1 - (int) width, x1 - (int) width, y1, borderColor);
        disableGL2D();
    }

    public static void drawBorderedRectGui(int x, int y, int x1, int y1, float width, int internalColor, int borderColor) {
        enableGL2D();
        Gui.drawRect(x + (int) width, y + (int) width, x1 - (int) width, y1 - (int) width, internalColor);
        Gui.drawRect(x + (int) width - 1, y, x1 - (int) width + 1, y + (int) width, borderColor);
        Gui.drawRect(x, y, x + (int) width, y1, borderColor);
        Gui.drawRect(x1 - (int) width, y, x1, y1, borderColor);
        Gui.drawRect(x + (int) width - 1, y1 - (int) width, x1 - (int) width + 1, y1, borderColor);
        disableGL2D();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ);
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ);
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ);
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ);
        tessellator.draw();
    }

    public static void drawOutlinedEntityESP(double x, double y, double z, double width, double height, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawBorderedRectReliant(float x, float y, float x1, float y1, float lineWidth, int inside, int border) {
        enableGL2D();
        drawGuiRect((double) x, (double) y, (double) x1, (double) y1, inside);
        glColor(border);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(3);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        disableGL2D();
    }

    public static void drawBorderedRectReliantGui(double x, double y, double x1, double y1, float lineWidth, int inside, int border) {
        enableGL2D();
        fakeGuiRect(x, y, x1, y1, inside);
        glColor(border);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(3);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y1);
        GL11.glVertex2d(x1, y1 + 0.5D);
        GL11.glVertex2d(x1, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        disableGL2D();
    }

    public static void renderCircleWithHoleInCenter(Vector2f center, float radiusOuter, float radiusInner, float[] color, float angle, float step) {
        float p1X = (float) ((double) center.x + Math.sin((double) angle) * (double) radiusOuter);
        float p1Y = (float) ((double) center.y + Math.cos((double) angle) * (double) radiusOuter);
        float p2X = (float) ((double) center.x + Math.sin((double) angle) * (double) radiusInner);
        float p2Y = (float) ((double) center.y + Math.cos((double) angle) * (double) radiusInner);
        float p3X = (float) ((double) center.x + Math.sin((double) (angle + step)) * (double) radiusInner);
        float p3Y = (float) ((double) center.y + Math.cos((double) (angle + step)) * (double) radiusInner);
        float p4X = (float) ((double) center.x + Math.sin((double) (angle + step)) * (double) radiusOuter);
        float p4Y = (float) ((double) center.y + Math.cos((double) (angle + step)) * (double) radiusOuter);
        float alpha = color[3];
        float red = color[0];
        float blue = color[1];
        float green = color[2];
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        worldRenderer.endVertex();
        worldRenderer.pos((double) p1X, (double) p1Y, 0.0D);
        worldRenderer.pos((double) p2X, (double) p2Y, 0.0D);
        worldRenderer.pos((double) p3X, (double) p3Y, 0.0D);
        worldRenderer.pos((double) p4X, (double) p4Y, 0.0D);
    }

    public static void drawHoloRect(double x, double y, double x1, double y1, float lineWidth, int color) {
        enableGL2D();
        drawGuiRect(x + (double) ((int) lineWidth) - 1.0D, y, x1 - (double) ((int) lineWidth) + 1.0D, y + (double) ((int) lineWidth), color);
        drawGuiRect(x, y, x + (double) ((int) lineWidth), y1, color);
        drawGuiRect(x1 - (double) ((int) lineWidth), y, x1, y1, color);
        drawGuiRect(x + (double) ((int) lineWidth) - 1.0D, y1 - (double) ((int) lineWidth), x1 - (double) ((int) lineWidth) + 1.0D, y1, color);
        disableGL2D();
    }

    public static void drawRect(Rectangle rectangle, int color) {
        drawRect((float) rectangle.x, (float) rectangle.y, (float) (rectangle.x + rectangle.width), (float) (rectangle.y + rectangle.height), color);
    }

    public static void drawRect(float x, float y, float x1, float y1, int color) {
        enableGL2D();
        glColor(color);
        drawRect(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawRectFourColor(double d, double e, double f, double g, float red, float green, float blue, float alpha) {
        enableGL2D();
        GL11.glColor4f(red, green, blue, alpha);
        drawRectDouble(d, e, f, g);
        disableGL2D();
    }

    public static void drawRectDouble(double x, double y, double x1, double y1, int color) {
        enableGL2D();
        glColor(color);
        drawRectDouble(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawRectDoubleJavaColor(double x, double y, double x1, double y1, int red, int green, int blue) {
        enableGL2D();
        GL11.glColor3f((float) red, (float) green, (float) blue);
        drawRectDouble(x, y, x1, y1);
        disableGL2D();
    }

    public static void glColor(int hex) {
        float alpha = (float) (hex >> 24 & 255) / 255.0F;
        float red = (float) (hex >> 16 & 255) / 255.0F;
        float green = (float) (hex >> 8 & 255) / 255.0F;
        float blue = (float) (hex & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawRect(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void drawRectDouble(double x, double y, double x1, double y1) {
        GL11.glBegin(7);
        GL11.glVertex2d(x, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void drawRectDoublePlayerESP(double x, double y, double x1, double y1, int nameColor) {
        float alpha = (float) (nameColor >> 24 & 255) / 255.0F;
        float red = (float) (nameColor >> 16 & 255) / 255.0F;
        float green = (float) (nameColor >> 8 & 255) / 255.0F;
        float blue = (float) (nameColor & 255) / 255.0F;

        GL11.glBegin(7);
        GL11.glVertex2d(x, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void enableGL3D(float lineWidth) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(lineWidth);
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void enableGL3D() {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4353);
        GL11.glDisable(2896);
    }

    public static void disableGL3D() {
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawBorderedRect(int x, int y, int x1, int y1, int insideC, int borderC) {
        enableGL2D();
        x = (int) ((float) x * 2.0F);
        x1 = (int) ((float) x1 * 2.0F);
        y = (int) ((float) y * 2.0F);
        y1 = (int) ((float) y1 * 2.0F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        drawVLine(x, y, y1 - 1, borderC);
        drawVLine(x1 - 1, y, y1, borderC);
        drawHLine(x, x1 - 1, y, borderC);
        drawHLine(x, x1 - 2, y1 - 1, borderC);
        Gui.drawRect(x + 1, y + 1, x1 - 1, y1 - 1, insideC);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        disableGL2D();
    }

    public static void drawHLine(int x, int y, int x1, int y1) {
        if (y < x) {
            int i = x;

            x = y;
            y = i;
        }

        Gui.drawRect(x, x1, y + 1, x1 + 1, y1);
    }

    public static void drawVLine(int x, int y, int x1, int y1) {
        if (x1 < y) {
            int i = y;

            y = x1;
            x1 = i;
        }

        Gui.drawRect(x, y + 1, x + 1, x1, y1);
    }

    public static void drawHLine(float x, float y, float x1, int y1, int y2) {
        if (y < x) {
            float f = x;

            x = y;
            y = f;
        }

        drawGradientRect((double) x, (double) x1, (double) (y + 1.0F), (double) (x1 + 1.0F), y1, y2);
    }

    public static void drawTracerLine(double[] pos, float[] c, float width) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(width);
        GL11.glColor4f(c[0], c[1], c[2], c[3]);
        GL11.glBegin(1);
        GL11.glVertex3d(0.0D, (double) Minecraft.getMinecraft().player.getEyeHeight(), 0.0D);
        GL11.glVertex3d(pos[0], pos[1], pos[2]);
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.endVertex();
        bufferbuilder.color(f1, f2, f3, f);
        bufferbuilder.pos(right, top, 0.0D);
        bufferbuilder.pos(left, top, 0.0D);
        bufferbuilder.color(f5, f6, f7, f4);
        bufferbuilder.pos(left, bottom, 0.0D);
        bufferbuilder.pos(right, bottom, 0.0D);
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawCircle(int x, int y, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);

        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d((double) x + Math.sin((double) i * 3.141592653589793D / 180.0D) * (double) radius, (double) y + Math.cos((double) i * 3.141592653589793D / 180.0D) * (double) radius);
        }

        GL11.glEnd();
    }

    public static void drawUnfilledCircle(int x, int y, float radius, float lineWidth, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(2848);
        GL11.glBegin(2);

        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d((double) x + Math.sin((double) i * 3.141592653589793D / 180.0D) * (double) radius, (double) y + Math.cos((double) i * 3.141592653589793D / 180.0D) * (double) radius);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
    }

    public static void drawFilledCircle(int x, int y, double r, int c) {
        float f = (float) (c >> 24 & 255) / 255.0F;
        float f1 = (float) (c >> 16 & 255) / 255.0F;
        float f2 = (float) (c >> 8 & 255) / 255.0F;
        float f3 = (float) (c & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(6);

        for (int i = 0; i <= 360; ++i) {
            double x2 = Math.sin((double) i * 3.141592653589793D / 180.0D) * r;
            double y2 = Math.cos((double) i * 3.141592653589793D / 180.0D) * r;

            GL11.glVertex2d((double) x + x2, (double) y + y2);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public static void drawCircle(int x, int y, double radius, int color) {
        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(2);

        for (int i = 0; i <= 360; ++i) {
            double x2 = Math.sin((double) i * 3.141592653589793D / 180.0D) * radius;
            double y2 = Math.cos((double) i * 3.141592653589793D / 180.0D) * radius;

            GL11.glVertex2d((double) x + x2, (double) y + y2);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public static void wolfRamCircle(float x, float y, float radius, float lineWidth, int color) {
        GL11.glEnable(3042);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha == 0.0F ? 1.0F : alpha);
        GL11.glLineWidth(lineWidth);
        int vertices = (int) Math.min(Math.max(radius, 45.0F), 360.0F);

        GL11.glBegin(2);

        for (int i = 0; i < vertices; ++i) {
            double angleRadians = 6.283185307179586D * (double) i / (double) vertices;

            GL11.glVertex2d((double) x + Math.sin(angleRadians) * (double) radius, (double) y + Math.cos(angleRadians) * (double) radius);
        }

        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glEnable(2884);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void wolfRamFilledCircle(float x, float y, float radius, int color) {
        GL11.glEnable(3042);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha == 0.0F ? 1.0F : alpha);
        int vertices = (int) Math.min(Math.max(radius, 45.0F), 360.0F);

        GL11.glBegin(9);

        for (int i = 0; i < vertices; ++i) {
            double angleRadians = 6.283185307179586D * (double) i / (double) vertices;

            GL11.glVertex2d((double) x + Math.sin(angleRadians) * (double) radius, (double) y + Math.cos(angleRadians) * (double) radius);
        }

        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glEnable(2884);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        wolfRamCircle(x, y, radius, 1.5F, 16777215);
    }

    public static void drawGuiRect(double x1, double y1, double x2, double y2, int color) {
        float red = (float) (color >> 24 & 255) / 255.0F;
        float green = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float alpha = (float) (color & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(green, blue, alpha, red);
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void fakeGuiRect(double left, double top, double right, double bottom, int color) {
        double f3;

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
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
