package live.rift.module.modules.misc;

import java.text.DecimalFormat;
import live.rift.module.Category;
import live.rift.module.Module;
import live.rift.setting.Setting;
import live.rift.util.TickRateManager;
import live.rift.util.Timer;

public class TimerMod extends Module {

    public Setting speed = new Setting("Speed", this, 1.0D, 0.1D, 40.0D, false);
    public Setting tpssync = new Setting("TPSSync", this, false);
    private Timer timer = new Timer();
    private float OverrideSpeed = 1.0F;
    private DecimalFormat l_Format = new DecimalFormat("#.#");

    public TimerMod() {
        super("TimerMod", 0, Category.MISC);
    }

    public void onDisable() {
        super.onDisable();
        TimerMod.mc.timer.tickLength = 50.0F;
    }

    public String modInf() {
        if (this.OverrideSpeed != 1.0F) {
            return String.valueOf(this.OverrideSpeed);
        } else if (this.tpssync.getValBoolean()) {
            float l_TPS = TickRateManager.Get().getTickRate();

            return this.l_Format.format((double) (l_TPS / 20.0F));
        } else {
            return this.l_Format.format((double) this.GetSpeed());
        }
    }

    public void onUpdate() {
        if (!this.getModInfo().equals(this.modInf())) {
            this.setModInfo(this.modInf());
        }

        if (this.OverrideSpeed != 1.0F && this.OverrideSpeed > 0.1F) {
            TimerMod.mc.timer.tickLength = 50.0F / this.OverrideSpeed;
        } else {
            if (this.tpssync.getValBoolean()) {
                float l_TPS = TickRateManager.Get().getTickRate();

                TimerMod.mc.timer.tickLength = Math.min(500.0F, 50.0F * (20.0F / l_TPS));
            } else {
                TimerMod.mc.timer.tickLength = 50.0F / this.GetSpeed();
            }

        }
    }

    private float GetSpeed() {
        return (float) Math.max(this.speed.getValDouble(), 0.10000000149011612D);
    }

    public void SetOverrideSpeed(float f) {
        this.OverrideSpeed = f;
    }
}
