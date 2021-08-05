package live.rift.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import live.rift.event.events.RenderEvent;
import live.rift.setting.Setting;
import live.rift.util.RiftRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;

public class ModuleManager {

    public ArrayList modules = new ArrayList();

    public ModuleManager() {
        Reflections reflections = new Reflections("live.rift.module.modules", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(Module.class);
        Iterator lengthComp = classes.iterator();

        while (lengthComp.hasNext()) {
            Class m = (Class) lengthComp.next();

            try {
                this.modules.add(m.newInstance());
            } catch (InstantiationException instantiationexception) {
                System.out.println("[Rift] Cannot create class instance of " + m.getName());
            } catch (IllegalAccessException illegalaccessexception) {
                System.out.println("[Rift] Cannot access Module class " + m.getName());
            }
        }

        Comparator lengthComp1 = new Comparator() {
            public int compare(Module o1, Module o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        };

        Collections.sort(this.modules, lengthComp1);
        this.addBinds();
    }

    public void addBinds() {
        Iterator iterator = this.modules.iterator();

        while (iterator.hasNext()) {
            Module m = (Module) iterator.next();

            new Setting("Bind", m, 0);
        }

    }

    public void onWorldRender(RenderWorldLastEvent event) {
        Minecraft.getMinecraft().profiler.startSection("rift");
        Minecraft.getMinecraft().profiler.startSection("setup");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);
        Vec3d renderPos = getInterpolatedPos(Minecraft.getMinecraft().player, event.getPartialTicks());
        RenderEvent e = new RenderEvent(RiftRenderer.INSTANCE, renderPos);

        e.resetTranslation();
        Minecraft.getMinecraft().profiler.endSection();
        this.getEnabledModules().stream().filter(test<invokedynamic>()).forEach(accept<invokedynamic>(e));
        Minecraft.getMinecraft().profiler.startSection("release");
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        RiftRenderer.releaseGL();
        Minecraft.getMinecraft().profiler.endSection();
        Minecraft.getMinecraft().profiler.endSection();
    }

    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return (new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)).add(getInterpolatedAmount(entity, (double) ticks));
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec) {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public ArrayList getEnabledModules() {
        ArrayList enabledModules = new ArrayList();
        Iterator iterator = this.modules.iterator();

        while (iterator.hasNext()) {
            Module module = (Module) iterator.next();

            if (module.getState()) {
                enabledModules.add(module);
            }
        }

        return enabledModules;
    }

    public Module getModule(String name) {
        Module the_module = null;
        Iterator iterator = this.modules.iterator();

        while (iterator.hasNext()) {
            Module module = (Module) iterator.next();

            if (module.name.equalsIgnoreCase(name)) {
                the_module = module;
                break;
            }
        }

        return the_module;
    }

    public boolean hasModules(Category category) {
        ArrayList m = new ArrayList();
        Iterator iterator = m.iterator();

        while (iterator.hasNext()) {
            Module module = (Module) iterator.next();

            if (module.getCategory().equals(category)) {
                m.add(module);
            }
        }

        if (m.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList getModulesByCategory(Category category) {
        ArrayList modules = new ArrayList();
        Iterator iterator = modules.iterator();

        while (iterator.hasNext()) {
            Module module = (Module) iterator.next();

            if (module.getCategory().equals(category)) {
                modules.add(module);
            }
        }

        return modules;
    }

    private static void lambda$onWorldRender$1(RenderEvent e, Module module) {
        Minecraft.getMinecraft().profiler.startSection(module.getName());
        module.onWorld(e);
        Minecraft.getMinecraft().profiler.endSection();
    }

    private static boolean lambda$onWorldRender$0(Module module) {
        return module.isEnabled();
    }
}
