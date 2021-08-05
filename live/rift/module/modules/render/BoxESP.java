package live.rift.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import live.rift.RiftMod;
import live.rift.event.events.RenderEvent;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.module.ModuleManager;
import live.rift.setting.Setting;
import live.rift.util.RainbowUtil;
import live.rift.util.RiftRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class BoxESP extends Module {

    public Setting ws = new Setting("Width", this, 1.0D, 0.10000000149011612D, 10.0D, false);
    public Setting players = new Setting("Players", this, true);
    public Setting items = new Setting("Items", this, true);
    public Setting itemNames = new Setting("Item Names", this, false);
    public Setting xp = new Setting("Thrown XP", this, true);
    public Setting mode;
    ArrayList options = new ArrayList();
    RainbowUtil rutil = new RainbowUtil(9);
    int ri = 0;

    public BoxESP() {
        super("BoxESP", 0, Category.RENDER);
        this.options.add("Fill");
        this.options.add("Outline");
        this.options.add("Both");
        this.mode = new Setting("Mode", this, "Fill", this.options);
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

    public void onRender() {
        this.rutil.onRender();
        if (this.ri >= 355) {
            this.ri = 0;
        }

    }

    public void onWorld(RenderEvent event) {
        String s = this.mode.getValString();
        byte b0 = -1;

        switch (s.hashCode()) {
        case 2076577:
            if (s.equals("Both")) {
                b0 = 2;
            }
            break;

        case 2189731:
            if (s.equals("Fill")) {
                b0 = 0;
            }
            break;

        case 558407714:
            if (s.equals("Outline")) {
                b0 = 1;
            }
        }

        switch (b0) {
        case 0:
            BoxESP.mc.world.loadedEntityList.stream().filter((entity) -> {
                return entity != BoxESP.mc.player;
            }).forEach((entities) -> {
                Vec3d pos = ModuleManager.getInterpolatedPos(entities, BoxESP.mc.getRenderPartialTicks());
                double x = pos.x - (entities.boundingBox.maxX - entities.boundingBox.minX) / 2.0D;
                double y = pos.y;
                double z = pos.z - (entities.boundingBox.maxZ - entities.boundingBox.minZ) / 2.0D;
                double w = entities.boundingBox.maxX - entities.boundingBox.minX;
                double h = entities.boundingBox.maxY - entities.boundingBox.minY;
                double l = entities.boundingBox.maxZ - entities.boundingBox.minZ;
                AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + w, y + h, z + l);

                RiftRenderer.prepare(7);
                if (this.players.getValBoolean() && entities instanceof EntityPlayer) {
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                }

                if (this.xp.getValBoolean() && entities instanceof EntityExpBottle) {
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                }

                if (this.items.getValBoolean() && entities instanceof EntityItem) {
                    bb = new AxisAlignedBB(x, y + 0.3D, z, x + w, y + 0.3D + h, z + l);
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                    if (this.itemNames.getValBoolean()) {
                        EntityItem i = (EntityItem) entities;
                        String name = i.getItem().getDisplayName() + " x" + i.getItem().stackSize;

                        GlStateManager.pushMatrix();
                        glBillboardDistanceScaled((float) x + 0.125F, (float) y + 0.125F, (float) z + 0.125F, BoxESP.mc.player, 0.1F);
                        GlStateManager.disableDepth();
                        GlStateManager.translate(-((double) BoxESP.mc.fontRenderer.getStringWidth(name) / 2.0D), 0.0D, 0.0D);
                        BoxESP.mc.fontRenderer.drawStringWithShadow(name, 0.0F, 0.0F, -197380);
                        GlStateManager.popMatrix();
                    }
                }

                RiftRenderer.release();
            });
            break;

        case 1:
            BoxESP.mc.world.loadedEntityList.stream().filter((entity) -> {
                return entity != BoxESP.mc.player;
            }).forEach((entities) -> {
                Vec3d pos = ModuleManager.getInterpolatedPos(entities, BoxESP.mc.getRenderPartialTicks());
                double x = pos.x - (entities.boundingBox.maxX - entities.boundingBox.minX) / 2.0D;
                double y = pos.y;
                double z = pos.z - (entities.boundingBox.maxZ - entities.boundingBox.minZ) / 2.0D;
                double w = entities.boundingBox.maxX - entities.boundingBox.minX;
                double h = entities.boundingBox.maxY - entities.boundingBox.minY;
                double l = entities.boundingBox.maxZ - entities.boundingBox.minZ;
                int rgb = this.getColor(this.ri);
                int r = rgb >> 16 & 255;
                int g = rgb >> 8 & 255;
                int b = rgb & 255;

                RiftRenderer.prepare(7);
                if (this.players.getValBoolean() && entities instanceof EntityPlayer) {
                    RiftRenderer.drawBoundingBox(x + 0.25D, y, z + 0.25D, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                }

                if (this.xp.getValBoolean() && entities instanceof EntityExpBottle) {
                    RiftRenderer.drawBoundingBox(x + 0.125D, y, z + 0.125D, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                }

                if (this.items.getValBoolean() && entities instanceof EntityItem) {
                    RiftRenderer.drawBoundingBox(x, y, z, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                    if (this.itemNames.getValBoolean()) {
                        EntityItem i = (EntityItem) entities;
                        String name = i.getItem().getDisplayName() + " x" + i.getItem().stackSize;

                        GlStateManager.pushMatrix();
                        glBillboardDistanceScaled((float) x + 0.125F, (float) y + 0.125F, (float) z + 0.125F, BoxESP.mc.player, 0.1F);
                        GlStateManager.disableDepth();
                        GlStateManager.translate(-((double) BoxESP.mc.fontRenderer.getStringWidth(name) / 2.0D), 0.0D, 0.0D);
                        BoxESP.mc.fontRenderer.drawStringWithShadow(name, 0.0F, 0.0F, -197380);
                        GlStateManager.popMatrix();
                    }
                }

                RiftRenderer.release();
            });
            break;

        case 2:
            BoxESP.mc.world.loadedEntityList.stream().filter((entity) -> {
                return entity != BoxESP.mc.player;
            }).forEach((entities) -> {
                Vec3d pos = ModuleManager.getInterpolatedPos(entities, BoxESP.mc.getRenderPartialTicks());
                double x = pos.x - (entities.boundingBox.maxX - entities.boundingBox.minX) / 2.0D;
                double y = pos.y;
                double z = pos.z - (entities.boundingBox.maxZ - entities.boundingBox.minZ) / 2.0D;
                double w = entities.boundingBox.maxX - entities.boundingBox.minX;
                double h = entities.boundingBox.maxY - entities.boundingBox.minY;
                double l = entities.boundingBox.maxZ - entities.boundingBox.minZ;
                AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + w, y + h, z + l);
                int rgb = this.getColor(this.ri);
                int r = rgb >> 16 & 255;
                int g = rgb >> 8 & 255;
                int b = rgb & 255;

                if (this.players.getValBoolean() && entities instanceof EntityPlayer) {
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawBoundingBox(x, y, z, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                    RiftRenderer.release();
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                    RiftRenderer.release();
                }

                if (this.xp.getValBoolean() && entities instanceof EntityExpBottle) {
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawBoundingBox(x, y, z, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                    RiftRenderer.release();
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                    RiftRenderer.release();
                }

                if (this.items.getValBoolean() && entities instanceof EntityItem) {
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawBoundingBox(x, y + 0.3D, z, w, l, h, this.ws.getValFloat(), r, g, b, 255);
                    RiftRenderer.release();
                    bb = new AxisAlignedBB(x, y + 0.3D, z, x + w, y + 0.3D + h, z + l);
                    RiftRenderer.prepare(7);
                    RiftRenderer.drawboxEntity(bb, this.getColor(this.ri), 50, 63);
                    RiftRenderer.release();
                    if (this.itemNames.getValBoolean()) {
                        EntityItem i = (EntityItem) entities;
                        String name = i.getItem().getDisplayName() + " x" + i.getItem().stackSize;

                        GlStateManager.pushMatrix();
                        glBillboardDistanceScaled((float) x + 0.125F, (float) y + 0.425F, (float) z + 0.125F, BoxESP.mc.player, 0.1F);
                        GlStateManager.disableDepth();
                        GlStateManager.translate(-((double) BoxESP.mc.fontRenderer.getStringWidth(name) / 2.0D), 0.0D, 0.0D);
                        BoxESP.mc.fontRenderer.drawStringWithShadow(name, 0.0F, 0.0F, -197380);
                        GlStateManager.popMatrix();
                    }
                }

            });
        }

    }

    public static void glBillboard(float x, float y, float z) {
        float scale = 0.02666667F;

        GlStateManager.translate((double) x - BoxESP.mc.getRenderManager().renderPosX, (double) y - BoxESP.mc.getRenderManager().renderPosY, (double) z - BoxESP.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-BoxESP.mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(BoxESP.mc.player.rotationPitch, BoxESP.mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
        glBillboard(x, y, z);
        int distance = (int) player.getDistance((double) x, (double) y, (double) z);
        float scaleDistance = (float) distance / 2.0F / (2.0F + (2.0F - scale));

        if (scaleDistance < 1.0F) {
            scaleDistance = 1.0F;
        }

        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }
}
