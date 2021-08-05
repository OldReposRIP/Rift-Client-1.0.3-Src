package live.rift.util;

import java.util.ArrayList;

public class RainbowUtil {

    private ArrayList CurrentRainbowIndexes = new ArrayList();
    private ArrayList RainbowArrayList = new ArrayList();
    private Timer RainbowSpeed = new Timer();
    private int m_Timer;

    public RainbowUtil(int p_Timer) {
        this.m_Timer = p_Timer;

        for (int l_I = 0; l_I < 360; ++l_I) {
            this.RainbowArrayList.add(Integer.valueOf(ColorUtil.GetRainbowColor((float) l_I, 90.0F, 50.0F, 1.0F).getRGB()));
            this.CurrentRainbowIndexes.add(Integer.valueOf(l_I));
        }

    }

    public int getTimer() {
        return this.m_Timer;
    }

    public int GetRainbowColorAt(int p_Index) {
        if (p_Index > this.CurrentRainbowIndexes.size() - 1) {
            p_Index = this.CurrentRainbowIndexes.size() - 1;
        }

        return ((Integer) this.RainbowArrayList.get(((Integer) this.CurrentRainbowIndexes.get(p_Index)).intValue())).intValue();
    }

    public void SetTimer(int p_NewTimer) {
        this.m_Timer = p_NewTimer;
    }

    public void onRender() {
        if (this.RainbowSpeed.passed((double) this.m_Timer)) {
            this.RainbowSpeed.reset();
            this.MoveListToNextColor();
        }

    }

    private void MoveListToNextColor() {
        if (!this.CurrentRainbowIndexes.isEmpty()) {
            this.CurrentRainbowIndexes.remove(this.CurrentRainbowIndexes.get(0));
            int l_Index = ((Integer) this.CurrentRainbowIndexes.get(this.CurrentRainbowIndexes.size() - 1)).intValue() + 1;

            if (l_Index >= this.RainbowArrayList.size() - 1) {
                l_Index = 0;
            }

            this.CurrentRainbowIndexes.add(Integer.valueOf(l_Index));
        }
    }
}
