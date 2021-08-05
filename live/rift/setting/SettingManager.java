package live.rift.setting;

import java.util.ArrayList;
import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.module.Module;

public class SettingManager {

    private ArrayList settings = new ArrayList();

    public void rSetting(Setting in) {
        this.settings.add(in);
    }

    public ArrayList getSettings() {
        return this.settings;
    }

    public ArrayList getSettingsByMod(Module mod) {
        ArrayList out = new ArrayList();
        Iterator iterator = this.getSettings().iterator();

        while (iterator.hasNext()) {
            Setting s = (Setting) iterator.next();

            if (s.getParentMod().equals(mod)) {
                out.add(s);
            }
        }

        if (out.isEmpty()) {
            return null;
        } else {
            return out;
        }
    }

    public Setting getSetting(String module, String name) {
        Iterator iterator = this.getSettings().iterator();

        Setting set;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            set = (Setting) iterator.next();
        } while (!set.getName().equalsIgnoreCase(name) || !set.getParentMod().getName().equalsIgnoreCase(name));

        return set;
    }

    public Setting getSettingByName(String name) {
        Iterator iterator = this.getSettings().iterator();

        Setting set;

        do {
            if (!iterator.hasNext()) {
                System.err.println("[Rift] Error Setting NOT found: \'" + name + "\'!");
                return null;
            }

            set = (Setting) iterator.next();
        } while (!set.getName().equalsIgnoreCase(name));

        return set;
    }

    public Setting getSettingByNameMod(String name, String mod) {
        return RiftMod.fevents.moduleManager.getModule(mod).getSetting(name);
    }

    public Setting getSettingByMod(String name, Module mod) {
        Iterator iterator = this.getSettings().iterator();

        Setting set;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            set = (Setting) iterator.next();
        } while (!set.getName().equalsIgnoreCase(name) || set.getParentMod() != mod);

        return set;
    }
}
