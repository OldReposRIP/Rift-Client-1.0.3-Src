package live.rift.setting;

import java.util.ArrayList;
import live.rift.module.Module;

public class Setting {

    private String name;
    private Module parent;
    private String mode;
    private String sval;
    private ArrayList options;
    private boolean bval;
    private double dval;
    private double min;
    private double max;
    private boolean onlyint = false;
    private int keyCode;

    public Setting(String name, Module parent, String sval, ArrayList options) {
        this.name = name;
        this.parent = parent;
        this.sval = sval;
        this.options = options;
        this.mode = "Combo";
        parent.addSetting(this);
    }

    public Setting(String name, Module parent, boolean bval) {
        this.name = name;
        this.parent = parent;
        this.bval = bval;
        this.mode = "Check";
        parent.addSetting(this);
    }

    public Setting(String name, Module parent, double dval, double min, double max, boolean onlyint) {
        this.name = name;
        this.parent = parent;
        this.dval = dval;
        this.min = min;
        this.max = max;
        this.onlyint = onlyint;
        this.mode = "Slider";
        parent.addSetting(this);
    }

    public Setting(String name, Module parent, int key) {
        this.name = name;
        this.parent = parent;
        this.keyCode = key;
        this.mode = "Bind";
        parent.addSetting(this);
    }

    public String getName() {
        return this.name;
    }

    public Module getParentMod() {
        return this.parent;
    }

    public String getValString() {
        return this.sval;
    }

    public int getKeyBind() {
        return this.keyCode;
    }

    public void setValString(String in) {
        this.sval = in;
    }

    public ArrayList getOptions() {
        return this.options;
    }

    public void setValKey(int in) {
        this.keyCode = in;
    }

    public boolean getValBoolean() {
        return this.bval;
    }

    public void setValBoolean(boolean in) {
        this.bval = in;
    }

    public double getValDouble() {
        if (this.onlyint) {
            this.dval = (double) ((int) this.dval);
        }

        return this.dval;
    }

    public float getValFloat() {
        return (float) this.dval;
    }

    public void setValDouble(double in) {
        this.dval = in;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public boolean isCombo() {
        return this.mode.equalsIgnoreCase("Combo");
    }

    public boolean isCheck() {
        return this.mode.equalsIgnoreCase("Check");
    }

    public boolean isSlider() {
        return this.mode.equalsIgnoreCase("Slider");
    }

    public boolean isBind() {
        return this.mode.equalsIgnoreCase("Bind");
    }

    public boolean onlyInt() {
        return this.onlyint;
    }
}
