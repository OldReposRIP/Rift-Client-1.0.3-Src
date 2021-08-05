package live.rift.module.modules.render;

import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;

public class NoRender extends Module {

    public Setting skylight = new Setting("Skylight", this, true);
    public Setting fire = new Setting("Fire", this, true);
    public Setting lava;
    public Setting hurtCam = new Setting("NoHurtCam", this, true);
    public Setting bossStack;
    public Setting totem = new Setting("Totem Pop", this, true);
    public Setting pumpkin = new Setting("Pumpkin Overlay", this, true);
    public Setting nausea = new Setting("Portal Nausea", this, true);
    public Setting portal = new Setting("Portal", this, true);
    public Setting armor = new Setting("Armor", this, true);
    public static NoRender i = new NoRender();

    public NoRender() {
        super("NoRender", 0, Category.RENDER);
        NoRender.i = this;
    }
}
