package live.rift.gui.util;

import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.gui.elements.Panel;

public class PanelUtil {

    public static Panel getPanel(String name) {
        Panel panel = null;
        Iterator iterator = RiftMod.clickgui.panels.iterator();

        while (iterator.hasNext()) {
            Panel p = (Panel) iterator.next();

            if (p.category.categoryName.equalsIgnoreCase(name)) {
                panel = p;
                break;
            }
        }

        return panel;
    }
}
