package live.rift.command.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import live.rift.RiftMod;
import live.rift.command.Command;
import live.rift.message.Messages;
import live.rift.module.Module;
import live.rift.setting.Setting;

public class Set extends Command {

    public ArrayList moduleNames = new ArrayList();

    public Set() {
        this.cmd = "set";
        Iterator iterator = RiftMod.fevents.moduleManager.modules.iterator();

        while (iterator.hasNext()) {
            Module m = (Module) iterator.next();

            this.aliases.add(m.getName().toLowerCase());
            this.moduleNames.add(m.getName().toLowerCase());
        }

        this.desc = "Set module settings";
    }

    public void handleCommand(String msg, List args) {
        if (args.size() < 3) {
            Messages.sendMessage("&cUsage: Set {ModuleName} {Setting} {Value}");
            Messages.sendMessage("&cUsage: {ModuleName} {Setting} {Value}");
        } else {
            int i = 0;

            if (((String) args.get(0)).equalsIgnoreCase("Set")) {
                ++i;
            }

            boolean c = false;
            Module m = RiftMod.fevents.moduleManager.getModule((String) args.get(0 + i));
            Setting v = null;
            Iterator value = m.getSettings().iterator();

            while (value.hasNext()) {
                Setting e = (Setting) value.next();

                if (((String) args.get(1 + i)).equalsIgnoreCase(e.getName().replaceAll("\\s", ""))) {
                    v = e;
                    c = true;
                    break;
                }
            }

            if (!c) {
                Messages.sendMessage("&cError: No setting " + (String) args.get(1) + " in module " + m.getName());
            } else {
                String s = (String) args.get(2 + i);

                if (v.isSlider()) {
                    try {
                        float f = Float.parseFloat(s);

                        if (v.getMin() <= (double) f && v.getMax() >= (double) f) {
                            if (v.onlyInt()) {
                                v.setValDouble(Math.floor((double) f));
                            } else {
                                v.setValDouble((double) f);
                            }

                            Messages.sendMessage("Set " + m.getName() + " " + v.getName() + " to " + s);
                        } else {
                            Messages.sendMessage("&cError: Third argument out of bounds");
                        }
                    } catch (Exception exception) {
                        Messages.sendMessage("&cError: Third argument must be an integer or float value");
                    }
                } else if (v.isCheck()) {
                    try {
                        boolean flag = false;

                        if (s.equalsIgnoreCase("on")) {
                            v.setValBoolean(true);
                            Messages.sendMessage("Set " + m.getName() + " " + v.getName() + " to true");
                        } else if (s.equalsIgnoreCase("off")) {
                            v.setValBoolean(false);
                            Messages.sendMessage("Set " + m.getName() + " " + v.getName() + " to false");
                        } else {
                            flag = Boolean.parseBoolean(s);
                            v.setValBoolean(flag);
                            Messages.sendMessage("Set " + m.getName() + " " + v.getName() + " to " + s);
                        }
                    } catch (Exception exception1) {
                        Messages.sendMessage("&cError: Third argument must be on, off, true, or false");
                    }
                } else if (v.isCombo()) {
                    Iterator iterator = v.getOptions().iterator();

                    String s;

                    do {
                        if (!iterator.hasNext()) {
                            Messages.sendMessage("&cError: Third argument must be one of the following: " + v.getOptions().toString());
                            return;
                        }

                        s = (String) iterator.next();
                    } while (!s.equalsIgnoreCase(s));

                    v.setValString(s);
                    Messages.sendMessage("Set " + m.getName() + " " + v.getName() + " to " + s);
                }
            }
        }
    }
}
