package live.rift.command;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class Command {

    public Minecraft mc = Minecraft.getMinecraft();
    public String cmd;
    public ArrayList aliases = new ArrayList();
    public String desc;
    public List arguments = new ArrayList();

    public void handleCommand(String msg, List args) {}

    public String getPre() {
        return this.cmd;
    }

    public boolean argExists(List args, int i) {
        return args.size() - 1 >= i;
    }
}
