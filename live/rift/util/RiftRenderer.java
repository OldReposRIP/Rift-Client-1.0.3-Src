package live.rift.util;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import live.rift.RiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RiftRenderer extends Tessellator {

    public static RiftRenderer INSTANCE = new RiftRenderer();
    public static final HashMap FACEMAP = new HashMap();
    static RainbowUtil rutil;

    public RiftRenderer() {
        super(2097152);
    }

    public static void prepare(int mode) {
        prepareGL();
        begin(mode);
    }

    public static void prepareGL() {
        GL11.glBlendFunc(770, 771);
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
    }

    public static double[] rPos() {
        try {
            return new double[] { ((Double) ((Field) Objects.requireNonNull(ReflectUtils.getField(RenderManager.class, new String[] { "renderPosX", "renderPosX"}))).get(Minecraft.getMinecraft().getRenderManager())).doubleValue(), ((Double) ((Field) Objects.requireNonNull(ReflectUtils.getField(RenderManager.class, new String[] { "renderPosY", "renderPosY"}))).get(Minecraft.getMinecraft().getRenderManager())).doubleValue(), ((Double) ((Field) Objects.requireNonNull(ReflectUtils.getField(RenderManager.class, new String[] { "renderPosZ", "renderPosZ"}))).get(Minecraft.getMinecraft().getRenderManager())).doubleValue()};
        } catch (IllegalAccessException illegalaccessexception) {
            illegalaccessexception.printStackTrace();
            return new double[] { 0.0D, 0.0D, 0.0D};
        }
    }

    public static void begin(int mode) {
        RiftRenderer.INSTANCE.getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public static void release() {
        render();
        releaseGL();
    }

    public static void render() {
        RiftRenderer.INSTANCE.draw();
    }

    public static void releaseGL() {
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
    }

    public static void glSetup() {
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
    }

    public static void glCleanup() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawBox(BlockPos blockPos, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(blockPos, r, g, b, a, sides);
    }

    public static void drawBoxOpacity(BlockPos blockPos, int argb, int opacity, int sides) {
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(blockPos, r, g, b, opacity, sides);
    }

    public static void drawBox(float x, float y, float z, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(RiftRenderer.INSTANCE.getBuffer(), x, y, z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
        drawBox(RiftRenderer.INSTANCE.getBuffer(), (float) blockPos.x, (float) blockPos.y, (float) blockPos.z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, float width, int sides) {
        drawBox(RiftRenderer.INSTANCE.getBuffer(), (float) blockPos.x, (float) blockPos.y, (float) blockPos.z, width, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static BufferBuilder getBufferBuilder() {
        return RiftRenderer.INSTANCE.getBuffer();
    }

    public static void drawBox(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & 1) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 2) != 0) {
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 4) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 8) != 0) {
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 16) != 0) {
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 32) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

    }

    public static void drawLines(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & 17) != 0) {
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 18) != 0) {
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 33) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 34) != 0) {
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 5) != 0) {
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 6) != 0) {
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 9) != 0) {
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 10) != 0) {
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 20) != 0) {
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 36) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 24) != 0) {
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 40) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

    }

    public static void drawBoundingBoxBlockPos(BlockPos bp, float width, int r, int g, int b, int alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        Minecraft mc = Minecraft.getMinecraft();
        double x = (double) bp.x - mc.getRenderManager().viewerPosX;
        double y = (double) bp.y - mc.getRenderManager().viewerPosY;
        double z = (double) bp.z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawOutlinedBox(AxisAlignedBB axisAlignedBB) {
        GL11.glBegin(1);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
        GL11.glEnd();
    }

    public static void drawBoundingBox(double x, double y, double z, double w, double l, double h, float width, int r, int g, int b, int alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        Minecraft mc = Minecraft.getMinecraft();
        double rx = x - mc.getRenderManager().viewerPosX;
        double ry = y - mc.getRenderManager().viewerPosY;
        double rz = z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(rx, ry, rz, rx + w, ry + h, rz + l);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBoxBottomBlockPos(BlockPos bp, float width, int r, int g, int b, int alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        Minecraft mc = Minecraft.getMinecraft();
        double x = (double) bp.x - mc.getRenderManager().viewerPosX;
        double y = (double) bp.y - mc.getRenderManager().viewerPosY;
        double z = (double) bp.z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawOTwoBox(BlockPos blockPos, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(blockPos, r, g, b, a, sides);
    }

    public static void drawOTwoBox(double x, double y, double z, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawHalfBox(RiftRenderer.INSTANCE.getBuffer(), x, y, z, 1.0F, 0.1F, 1.0F, r, g, b, a, sides);
    }

    public static void drawOTwoBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
        drawHalfBox(RiftRenderer.INSTANCE.getBuffer(), (double) blockPos.x, (double) blockPos.y, (double) blockPos.z, 1.0F, 0.1F, 1.0F, r, g, b, a, sides);
    }

    public static void drawOTwoBox(BufferBuilder buffer, double x, double y, double z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & 1) != 0) {
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 2) != 0) {
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

        if ((sides & 4) != 0) {
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 8) != 0) {
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

        if ((sides & 16) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 32) != 0) {
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

    }

    public static void drawHalfBox(BlockPos blockPos, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(blockPos, r, g, b, a, sides);
    }

    public static void drawHalfBox(double x, double y, double z, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawHalfBox(RiftRenderer.INSTANCE.getBuffer(), x, y, z, 1.0F, 0.5F, 1.0F, r, g, b, a, sides);
    }

    public static void drawHalfBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
        drawHalfBox(RiftRenderer.INSTANCE.getBuffer(), (double) blockPos.x, (double) blockPos.y, (double) blockPos.z, 1.0F, 0.5F, 1.0F, r, g, b, a, sides);
    }

    public static void drawHalfBox(BufferBuilder buffer, double x, double y, double z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & 1) != 0) {
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 2) != 0) {
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

        if ((sides & 4) != 0) {
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 8) != 0) {
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

        if ((sides & 16) != 0) {
            buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x, y + (double) h, z).color(r, g, b, a).endVertex();
        }

        if ((sides & 32) != 0) {
            buffer.pos(x + (double) w, y, z + (double) d).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z).color(r, g, b, a).endVertex();
            buffer.pos(x + (double) w, y + (double) h, z + (double) d).color(r, g, b, a).endVertex();
        }

    }

    public static void drawOTwoBoundingBoxBlockPos(BlockPos bp, float width, int r, int g, int b, int alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        Minecraft mc = Minecraft.getMinecraft();
        double x = (double) bp.x - mc.getRenderManager().viewerPosX;
        double y = (double) bp.y - mc.getRenderManager().viewerPosY;
        double z = (double) bp.z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0D, y + 0.1D, z + 1.0D);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawboxEntity(AxisAlignedBB bb, int argb, int a, int sides) {
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawboxEntity(RiftRenderer.INSTANCE.getBuffer(), bb, r, g, b, a, sides);
    }

    public static void drawboxEntity(BlockPos blockPos, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawboxEntity(blockPos, r, g, b, a, sides);
    }

    public static void drawboxEntity(float x, float y, float z, int argb, int sides) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;

        drawBox(RiftRenderer.INSTANCE.getBuffer(), x, y, z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static void drawboxEntity(BlockPos blockPos, int r, int g, int b, int a, int sides) {
        drawBox(RiftRenderer.INSTANCE.getBuffer(), (float) blockPos.getX(), (float) blockPos.getY(), (float) blockPos.getZ(), 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static void drawboxEntity(Vec3d vec3d, int r, int g, int b, int a, int sides) {
        drawBox(RiftRenderer.INSTANCE.getBuffer(), (float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
    }

    public static void drawboxEntity(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & 1) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 2) != 0) {
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 4) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 8) != 0) {
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

        if ((sides & 16) != 0) {
            buffer.pos((double) x, (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) x, (double) (y + h), (double) z).color(r, g, b, a).endVertex();
        }

        if ((sides & 32) != 0) {
            buffer.pos((double) (x + w), (double) y, (double) (z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) y, (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) z).color(r, g, b, a).endVertex();
            buffer.pos((double) (x + w), (double) (y + h), (double) (z + d)).color(r, g, b, a).endVertex();
        }

    }

    public static void drawboxEntity(BufferBuilder buffer, AxisAlignedBB bb, int r, int g, int b, int a, int sides) {
        if ((sides & 1) != 0) {
            buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        }

        if ((sides & 2) != 0) {
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        }

        if ((sides & 4) != 0) {
            buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        }

        if ((sides & 8) != 0) {
            buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        }

        if ((sides & 16) != 0) {
            buffer.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        }

        if ((sides & 32) != 0) {
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        }

    }

    public static int getColor(int index) {
        boolean color = true;
        int color1;

        if (RiftMod.setmgr.getSettingByMod("Rainbow", RiftMod.fevents.moduleManager.getModule("Gui")).getValBoolean()) {
            color1 = RiftRenderer.rutil.GetRainbowColorAt(index);
        } else {
            color1 = (new Color((int) RiftMod.setmgr.getSettingByNameMod("Red", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Green", "Gui").getValDouble(), (int) RiftMod.setmgr.getSettingByNameMod("Blue", "Gui").getValDouble())).getRGB();
        }

        return color1;
    }

    public static void drawCSGOOutline(Entity e, int rgb) {
        GL11.glLineWidth(3.0F);
        GL11.glEnable(2848);
        float r = (float) (rgb >> 16 & 255);
        float g = (float) (rgb >> 8 & 255);
        float b = (float) (rgb & 255);

        r /= 255.0F;
        g /= 255.0F;
        b /= 255.0F;
        GL11.glColor4f(r, g, b, 0.5F);
        GL11.glBegin(2);
        GL11.glVertex2d((double) (-e.width), 0.0D);
        GL11.glVertex2d((double) (-e.width), (double) (e.height / 3.0F));
        GL11.glVertex2d((double) (-e.width), 0.0D);
        GL11.glVertex2d((double) (-e.width / 3.0F * 2.0F), 0.0D);
        GL11.glEnd();
        GL11.glBegin(2);
        GL11.glVertex2d((double) (-e.width), (double) e.height);
        GL11.glVertex2d((double) (-e.width / 3.0F * 2.0F), (double) e.height);
        GL11.glVertex2d((double) (-e.width), (double) e.height);
        GL11.glVertex2d((double) (-e.width), (double) (e.height / 3.0F * 2.0F));
        GL11.glEnd();
        GL11.glBegin(2);
        GL11.glVertex2d((double) e.width, (double) e.height);
        GL11.glVertex2d((double) (e.width / 3.0F * 2.0F), (double) e.height);
        GL11.glVertex2d((double) e.width, (double) e.height);
        GL11.glVertex2d((double) e.width, (double) (e.height / 3.0F * 2.0F));
        GL11.glEnd();
        GL11.glBegin(2);
        GL11.glVertex2d((double) e.width, 0.0D);
        GL11.glVertex2d((double) (e.width / 3.0F * 2.0F), 0.0D);
        GL11.glVertex2d((double) e.width, 0.0D);
        GL11.glVertex2d((double) e.width, (double) (e.height / 3.0F));
        GL11.glEnd();
    }

    static {
        RiftRenderer.FACEMAP.put(EnumFacing.DOWN, Integer.valueOf(1));
        RiftRenderer.FACEMAP.put(EnumFacing.WEST, Integer.valueOf(16));
        RiftRenderer.FACEMAP.put(EnumFacing.NORTH, Integer.valueOf(4));
        RiftRenderer.FACEMAP.put(EnumFacing.SOUTH, Integer.valueOf(8));
        RiftRenderer.FACEMAP.put(EnumFacing.EAST, Integer.valueOf(32));
        RiftRenderer.FACEMAP.put(EnumFacing.UP, Integer.valueOf(2));
        RiftRenderer.rutil = new RainbowUtil(9);
    }

    public static final class Line {

        public static final int DOWN_WEST = 17;
        public static final int UP_WEST = 18;
        public static final int DOWN_EAST = 33;
        public static final int UP_EAST = 34;
        public static final int DOWN_NORTH = 5;
        public static final int UP_NORTH = 6;
        public static final int DOWN_SOUTH = 9;
        public static final int UP_SOUTH = 10;
        public static final int NORTH_WEST = 20;
        public static final int NORTH_EAST = 36;
        public static final int SOUTH_WEST = 24;
        public static final int SOUTH_EAST = 40;
        public static final int ALL = 63;
    }

    public static final class Quad {

        public static final int DOWN = 1;
        public static final int UP = 2;
        public static final int NORTH = 4;
        public static final int SOUTH = 8;
        public static final int WEST = 16;
        public static final int EAST = 32;
        public static final int ALL = 63;
    }
}
