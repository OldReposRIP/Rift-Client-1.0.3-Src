package live.rift.module;

import java.util.ArrayList;
import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.config.Configuration;
import live.rift.event.events.RenderEvent;
import live.rift.setting.Setting;
import net.minecraft.client.Minecraft;

public class Module {

    public String name;
    public int key;
    public Category category;
    private boolean state;
    public String modInfo = "";
    protected static Minecraft mc = Minecraft.getMinecraft();
    Configuration config = new Configuration();
    public boolean visible;

    public Module(String name, int keyCode, Category cate) {
        this.key = keyCode;
        this.category = cate;
        this.name = name;

        new Setting("Visible", this, true);
        this.getName();
        this.load();
    }

    public Setting getSetting(String name) {
        return RiftMod.setmgr.getSettingByMod(name, this);
    }

    public ArrayList getSettings() {
        return RiftMod.setmgr.getSettingsByMod(this);
    }

    public void addSetting(Setting setting) {
        RiftMod.setmgr.rSetting(setting);
    }

    public Category getCategory() {
        return this.category;
    }

    public void load() {
        Iterator iterator = RiftMod.setmgr.getSettingsByMod(this).iterator();

        while (iterator.hasNext()) {
            Setting e = (Setting) iterator.next();

            if (e.isBind()) {
                this.key = e.getKeyBind();
            }

            if (e.getName().equalsIgnoreCase("Visible")) {
                this.visible = e.getValBoolean();
            }
        }

    }

    public boolean isVisible() {
        return this.getSetting("Visible").getValBoolean();
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int keyCode) {
        Module module = RiftMod.fevents.moduleManager.getModule(this.name);

        if (module != null) {
            Iterator iterator = RiftMod.setmgr.getSettingsByMod(this).iterator();

            while (iterator.hasNext()) {
                Setting e = (Setting) iterator.next();

                if (e.isBind()) {
                    this.key = keyCode;
                    e.setValKey(keyCode);
                }
            }
        }

    }

    public String getModInfo() {
        return this.modInfo;
    }

    public void setModInfo(String info) {
        this.modInfo = info;
    }

    public void onToggle(boolean state) {}

    public void onEnable() {}

    public void onDisable() {}

    public void onUpdate() {}

    public void onRender() {}

    public void onWorld(RenderEvent e) {}

    public void onKey(int keyCode) {
        if (keyCode == this.getKey()) {
            this.setState(!this.getState());
            this.onToggle(this.getState());
        }

    }

    public boolean isEnabled() {
        return this.state;
    }

    public boolean isDisabled() {
        return !this.state;
    }

    public void enable() {
        this.state = true;
    }

    public void disable() {
        this.state = false;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
        this.subscribeState(state);
    }

    public void subscribeState(boolean s) {
        if (s) {
            this.onEnable();
            RiftMod.EVENT_BUS.subscribe((Object) this);
        } else if (!s) {
            this.onDisable();
            RiftMod.EVENT_BUS.unsubscribe((Object) this);
        }

    }

    public String getName() {
        return this.name;
    }
}
