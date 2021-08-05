package live.rift.mixin;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public class Loader implements IFMLLoadingPlugin {

    private static boolean isObfuscatedEnvironment = false;

    public Loader() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.rift.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("name");
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map data) {
        Loader.isObfuscatedEnvironment = ((Boolean) data.get("runtimeDeobfuscationEnabled")).booleanValue();
    }

    public String getAccessTransformerClass() {
        return null;
    }
}
