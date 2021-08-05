package live.rift.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import live.rift.RiftMod;
import live.rift.gui.elements.Panel;
import live.rift.module.Module;
import live.rift.module.ModuleManager;
import live.rift.setting.Setting;
import live.rift.setting.SettingManager;
import net.minecraft.client.Minecraft;

public class Configuration {

    public void createConfig(ModuleManager mmanager, SettingManager mngr) {
        if (mmanager.modules != null) {
            Iterator iterator = mmanager.modules.iterator();

            while (iterator.hasNext()) {
                Module m = (Module) iterator.next();
                File dir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                File dir1 = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator);

                if (!dir1.exists()) {
                    dir1.mkdir();
                }

                File file = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator + m.name + ".txt");

                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                    }

                    if (mngr.getSettingsByMod(m) != null) {
                        this.makeSettings(m, file);
                    }
                } else if (mngr.getSettingsByMod(m) != null) {
                    Iterator e1 = RiftMod.setmgr.getSettingsByMod(m).iterator();

                    while (e1.hasNext()) {
                        Setting s = (Setting) e1.next();

                        if (!this.hasSettings(s)) {
                            this.makeSetting(s, file);
                        }
                    }
                }
            }
        }

    }

    public void makeSetting(Setting s, File f) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(f, true));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }

        if (writer != null) {
            if (s.isCheck()) {
                try {
                    writer.append("bool:" + s.getName() + ":" + (s.getValBoolean() ? "true" : "false") + "\r\n");
                } catch (IOException ioexception1) {
                    ioexception1.printStackTrace();
                }
            }

            if (s.isSlider()) {
                try {
                    if (s.onlyInt()) {
                        writer.append("sliderint:" + s.getName() + ":" + s.getValDouble() + "\r\n");
                    } else if (!s.onlyInt()) {
                        writer.append("sliderdouble:" + s.getName() + ":" + s.getValDouble() + "\r\n");
                    }
                } catch (IOException ioexception2) {
                    ioexception2.printStackTrace();
                }
            }

            if (s.isCombo() && s.getOptions() != null) {
                try {
                    String e = Character.toUpperCase(s.getValString().toLowerCase().charAt(0)) + s.getValString().toLowerCase().substring(1);

                    writer.append("combo:" + s.getName() + ":" + e + "\r\n");
                } catch (IOException ioexception3) {
                    ioexception3.printStackTrace();
                }
            }

            if (s.isBind()) {
                try {
                    writer.append("key:Bind:" + String.valueOf(s.getKeyBind()) + "\r\n");
                } catch (IOException ioexception4) {
                    ioexception4.printStackTrace();
                }
            }
        }

        try {
            writer.close();
        } catch (IOException ioexception5) {
            ioexception5.printStackTrace();
        }

    }

    public void makeSettings(Module m, File file) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }

        if (writer != null) {
            try {
                writer.write("module:enabled:" + (m.getState() ? "true" : "false") + "\r\n");
            } catch (IOException ioexception1) {
                ioexception1.printStackTrace();
            }

            Iterator e = RiftMod.setmgr.getSettingsByMod(m).iterator();

            while (e.hasNext()) {
                Setting s = (Setting) e.next();

                if (s.isCheck()) {
                    try {
                        writer.write("bool:" + s.getName() + ":" + (s.getValBoolean() ? "true" : "false") + "\r\n");
                    } catch (IOException ioexception2) {
                        ioexception2.printStackTrace();
                    }
                }

                if (s.isSlider()) {
                    try {
                        if (s.onlyInt()) {
                            writer.write("sliderint:" + s.getName() + ":" + s.getValDouble() + "\r\n");
                        } else if (!s.onlyInt()) {
                            writer.write("sliderdouble:" + s.getName() + ":" + s.getValDouble() + "\r\n");
                        }
                    } catch (IOException ioexception3) {
                        ioexception3.printStackTrace();
                    }
                }

                if (s.isCombo() && s.getOptions() != null) {
                    try {
                        writer.write("combo:" + s.getName() + ":" + s.getValString() + "\r\n");
                    } catch (IOException ioexception4) {
                        ioexception4.printStackTrace();
                    }
                }

                if (s.isBind()) {
                    try {
                        writer.write("key:Bind:" + String.valueOf(s.getParentMod().getKey()) + "\r\n");
                    } catch (IOException ioexception5) {
                        ioexception5.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("[RiftMod] Could not write Settings for Module " + m.name);
        }

        try {
            writer.close();
        } catch (IOException ioexception6) {
            ioexception6.printStackTrace();
        }

    }

    public boolean hasSettings(Setting set) {
        boolean has = false;

        try {
            BufferedReader e = new BufferedReader(new FileReader(new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator + set.getParentMod().name + ".txt")));
            Throwable throwable = null;

            try {
                String line;

                try {
                    while ((line = e.readLine()) != null) {
                        if (line.contains(":") && line.contains(set.getName())) {
                            has = true;
                        }
                    }
                } catch (Throwable throwable1) {
                    throwable = throwable1;
                    throw throwable1;
                }
            } finally {
                if (e != null) {
                    if (throwable != null) {
                        try {
                            e.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    } else {
                        e.close();
                    }
                }

            }
        } catch (FileNotFoundException filenotfoundexception) {
            filenotfoundexception.printStackTrace();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }

        return has;
    }

    public void saveSettings(ModuleManager mngr, SettingManager smngr) {
        if (mngr.modules != null) {
            Iterator iterator = mngr.modules.iterator();

            while (iterator.hasNext()) {
                Module m = (Module) iterator.next();
                File file = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator + m.name + ".txt");
                PrintWriter pw = null;

                try {
                    pw = new PrintWriter(file.getAbsolutePath());
                } catch (FileNotFoundException filenotfoundexception) {
                    filenotfoundexception.printStackTrace();
                }

                if (pw != null) {
                    pw.print("");
                    pw.close();
                }

                if (file.exists() && smngr.getSettingsByMod(m) != null) {
                    this.makeSettings(m, file);
                }
            }
        }

    }

    public void loadSettings(Module m) {
        if (this.existingSettings(m)) {
            try {
                BufferedReader e = new BufferedReader(new FileReader(new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator + m.name + ".txt")));
                Throwable throwable = null;

                try {
                    String line;

                    try {
                        while ((line = e.readLine()) != null) {
                            if (line.contains(":")) {
                                if (line.split(":")[0].equalsIgnoreCase("module")) {
                                    m.setState(Boolean.valueOf(line.split(":")[2]).booleanValue());
                                }

                                Iterator iterator = RiftMod.setmgr.getSettingsByMod(m).iterator();

                                while (iterator.hasNext()) {
                                    Setting s = (Setting) iterator.next();

                                    if (s.getName().equalsIgnoreCase(line.split(":")[1].split(":")[0])) {
                                        if (s.isCheck()) {
                                            s.setValBoolean(Boolean.valueOf(line.split(":")[2]).booleanValue());
                                        }

                                        if (s.isSlider()) {
                                            if (s.onlyInt()) {
                                                s.setValDouble(Double.valueOf(line.split(":")[2]).doubleValue());
                                            } else if (!s.onlyInt()) {
                                                s.setValDouble(Double.valueOf(line.split(":")[2]).doubleValue());
                                            }
                                        }

                                        if (s.isCombo()) {
                                            s.setValString(line.split(":")[2]);
                                        }

                                        if (s.isBind()) {
                                            s.getParentMod().setKey(Integer.valueOf(line.split(":")[2]).intValue());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable throwable1) {
                        throwable = throwable1;
                        throw throwable1;
                    }
                } finally {
                    if (e != null) {
                        if (throwable != null) {
                            try {
                                e.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        } else {
                            e.close();
                        }
                    }

                }
            } catch (FileNotFoundException filenotfoundexception) {
                filenotfoundexception.printStackTrace();
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }

    }

    public boolean existingSettings(Module module) {
        File fileSettings = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "settings" + File.separator + module.name + ".txt");

        return fileSettings.exists();
    }

    public boolean checkPanelDir() {
        File panelDir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "gui" + File.separator);

        return panelDir.exists();
    }

    public boolean checkModuleListDir() {
        File arrayDir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "arraylist" + File.separator);

        return arrayDir.exists();
    }

    public void savePanel(Panel p) {
        File panelFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "gui" + File.separator + p.category.categoryName + ".txt");
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(panelFile));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }

        if (writer != null) {
            try {
                writer.write("x:" + String.valueOf(p.x) + "\r\n");
                writer.write("y:" + String.valueOf(p.y) + "\r\n");
                writer.write("extended:" + String.valueOf(p.extended) + "\r\n");
            } catch (IOException ioexception1) {
                ioexception1.printStackTrace();
            }

            try {
                writer.close();
            } catch (IOException ioexception2) {
                ioexception2.printStackTrace();
            }
        }

    }

    public void loadPanels() {
        Panel p;

        if (RiftMod.clickgui.panels != null) {
            for (Iterator iterator = RiftMod.clickgui.panels.iterator(); iterator.hasNext(); p.addModules()) {
                p = (Panel) iterator.next();

                try {
                    BufferedReader e = new BufferedReader(new FileReader(new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "gui" + File.separator + p.category.categoryName + ".txt")));
                    Throwable throwable = null;

                    try {
                        String line;

                        try {
                            while ((line = e.readLine()) != null) {
                                if (line.contains(":")) {
                                    if (line.split(":")[0].equalsIgnoreCase("x")) {
                                        p.x = Integer.valueOf(line.split(":")[1]).intValue();
                                    }

                                    if (line.split(":")[0].equalsIgnoreCase("y")) {
                                        p.y = Integer.valueOf(line.split(":")[1]).intValue();
                                    }

                                    if (line.split(":")[0].equalsIgnoreCase("extended")) {
                                        p.extended = Boolean.valueOf(line.split(":")[1]).booleanValue();
                                    }
                                }
                            }
                        } catch (Throwable throwable1) {
                            throwable = throwable1;
                            throw throwable1;
                        }
                    } finally {
                        if (e != null) {
                            if (throwable != null) {
                                try {
                                    e.close();
                                } catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            } else {
                                e.close();
                            }
                        }

                    }
                } catch (FileNotFoundException filenotfoundexception) {
                    filenotfoundexception.printStackTrace();
                } catch (IOException ioexception) {
                    ioexception.printStackTrace();
                }
            }
        }

    }

    public void savePanels() {
        if (RiftMod.clickgui.panels != null) {
            Iterator iterator = RiftMod.clickgui.panels.iterator();

            while (iterator.hasNext()) {
                Panel p = (Panel) iterator.next();
                File file = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator + "RiftMod" + File.separator + "gui" + File.separator + p.category.categoryName + ".txt");
                PrintWriter pw = null;

                try {
                    pw = new PrintWriter(file.getAbsolutePath());
                } catch (FileNotFoundException filenotfoundexception) {
                    filenotfoundexception.printStackTrace();
                }

                if (pw != null) {
                    pw.print("");
                    pw.close();
                }

                if (file.exists()) {
                    this.savePanel(p);
                }
            }
        }

    }
}
