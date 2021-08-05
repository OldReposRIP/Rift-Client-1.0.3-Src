package live.rift.module.modules.render;

import live.rift.module.Category;
import live.rift.module.Module;

public class Chams extends Module {

    private static Chams INSTANCE = new Chams();

    public Chams() {
        super("Chams", 0, Category.RENDER);
        this.setInstance();
    }

    private void setInstance() {
        Chams.INSTANCE = this;
    }

    public static Chams getInstance() {
        if (Chams.INSTANCE == null) {
            Chams.INSTANCE = new Chams();
        }

        return Chams.INSTANCE;
    }
}
