package live.rift.util;

import java.awt.Color;

public class ColorUtil {

    public Color m_BaseColor;
    private float[] m_HSB;
    private float m_Alpha;

    public static String getColor(String bColor) {
        String uColor = "";
        byte b0 = -1;

        switch (bColor.hashCode()) {
        case 1226:
            if (bColor.equals("&0")) {
                b0 = 0;
            }
            break;

        case 1227:
            if (bColor.equals("&1")) {
                b0 = 1;
            }
            break;

        case 1228:
            if (bColor.equals("&2")) {
                b0 = 2;
            }
            break;

        case 1229:
            if (bColor.equals("&3")) {
                b0 = 3;
            }
            break;

        case 1230:
            if (bColor.equals("&4")) {
                b0 = 4;
            }
            break;

        case 1231:
            if (bColor.equals("&5")) {
                b0 = 5;
            }
            break;

        case 1232:
            if (bColor.equals("&6")) {
                b0 = 6;
            }
            break;

        case 1233:
            if (bColor.equals("&7")) {
                b0 = 7;
            }
            break;

        case 1234:
            if (bColor.equals("&8")) {
                b0 = 8;
            }
            break;

        case 1235:
            if (bColor.equals("&9")) {
                b0 = 9;
            }

        case 1236:
        case 1237:
        case 1238:
        case 1239:
        case 1240:
        case 1241:
        case 1242:
        case 1243:
        case 1244:
        case 1245:
        case 1246:
        case 1247:
        case 1248:
        case 1249:
        case 1250:
        case 1251:
        case 1252:
        case 1253:
        case 1254:
        case 1255:
        case 1256:
        case 1257:
        case 1258:
        case 1259:
        case 1260:
        case 1261:
        case 1262:
        case 1263:
        case 1264:
        case 1265:
        case 1266:
        case 1267:
        case 1268:
        case 1269:
        case 1270:
        case 1271:
        case 1272:
        case 1273:
        case 1274:
        default:
            break;

        case 1275:
            if (bColor.equals("&a")) {
                b0 = 10;
            }
            break;

        case 1276:
            if (bColor.equals("&b")) {
                b0 = 11;
            }
            break;

        case 1277:
            if (bColor.equals("&c")) {
                b0 = 12;
            }
            break;

        case 1278:
            if (bColor.equals("&d")) {
                b0 = 13;
            }
            break;

        case 1279:
            if (bColor.equals("&e")) {
                b0 = 14;
            }
            break;

        case 1280:
            if (bColor.equals("&f")) {
                b0 = 15;
            }
        }

        switch (b0) {
        case 0:
            uColor = "§0";
            break;

        case 1:
            uColor = "§1";
            break;

        case 2:
            uColor = "§2";
            break;

        case 3:
            uColor = "§3";
            break;

        case 4:
            uColor = "§4";
            break;

        case 5:
            uColor = "§5";
            break;

        case 6:
            uColor = "§6";
            break;

        case 7:
            uColor = "§7";
            break;

        case 8:
            uColor = "§8";
            break;

        case 9:
            uColor = "§9";
            break;

        case 10:
            uColor = "§a";
            break;

        case 11:
            uColor = "§b";
            break;

        case 12:
            uColor = "§c";
            break;

        case 13:
            uColor = "§d";
            break;

        case 14:
            uColor = "§e";
            break;

        case 15:
            uColor = "§f";
        }

        return uColor;
    }

    public ColorUtil(Color p_ColorBase) {
        this.m_BaseColor = p_ColorBase;
        this.m_HSB = GenerateHSB(p_ColorBase);
        this.m_Alpha = (float) p_ColorBase.getAlpha() / 255.0F;
    }

    public ColorUtil(float n, float n2, float n3) {
        this(n, n2, n3, 1.0F);
    }

    public ColorUtil(float[] array) {
        this(array, 1.0F);
    }

    public ColorUtil(float[] d, float k) {
        this.m_HSB = d;
        this.m_Alpha = k;
        this.m_BaseColor = GetRainbowColorFromArray(d, k);
    }

    public ColorUtil(float n, float n2, float n3, float k) {
        boolean n4 = true;
        float[] d = new float[] { n, n2, n3};

        this.m_HSB = d;
        this.m_Alpha = k;
        this.m_BaseColor = GetRainbowColorFromArray(this.m_HSB, k);
    }

    public String toString() {
        return (new StringBuilder()).insert(0, "HSLColor[h=").append(this.m_HSB[0]).append(",s=").append(this.m_HSB[1]).append(",l=").append(this.m_HSB[2]).append(",alpha=").append(this.m_Alpha).append("]").toString();
    }

    public Color GetColorWithLightnessMax(float max) {
        max = (100.0F - max) / 100.0F;
        max = Math.max(0.0F, this.m_HSB[2] * max);
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], max, this.m_Alpha);
    }

    public Color GetColorWithLightnessMin(float min) {
        min = (100.0F + min) / 100.0F;
        min = Math.min(100.0F, this.m_HSB[2] * min);
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], min, this.m_Alpha);
    }

    public float GetAlpha() {
        return this.m_Alpha;
    }

    public Color GetColorWithBrightness(float p_Brightness) {
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], p_Brightness, this.m_Alpha);
    }

    public float GetHue() {
        return this.m_HSB[0];
    }

    public float GetSaturation() {
        return this.m_HSB[1];
    }

    public float GetLightness() {
        return this.m_HSB[2];
    }

    public Color GetLocalColor() {
        return this.m_BaseColor;
    }

    public Color GetColorWithHue(float p_Hue) {
        return GetRainbowColor(p_Hue, this.m_HSB[1], this.m_HSB[2], this.m_Alpha);
    }

    public Color GetColorWithSaturation(float p_Saturation) {
        return GetRainbowColor(this.m_HSB[0], p_Saturation, this.m_HSB[2], this.m_Alpha);
    }

    public static float[] GenerateHSB(Color color) {
        float[] rgbColorComponents = color.getRGBColorComponents((float[]) null);
        float n = rgbColorComponents[0];
        float n2 = rgbColorComponents[1];
        float n3 = rgbColorComponents[2];
        float min = Math.min(n, Math.min(n2, n3));
        float max = Math.max(n, Math.max(n2, n3));
        float n4 = 0.0F;
        float n5;

        if (max == min) {
            n4 = 0.0F;
            n5 = max;
        } else if (max == n) {
            n4 = (60.0F * (n2 - n3) / (max - min) + 360.0F) % 360.0F;
            n5 = max;
        } else if (max == n2) {
            n4 = 60.0F * (n3 - n) / (max - min) + 120.0F;
            n5 = max;
        } else {
            if (max == n3) {
                n4 = 60.0F * (n - n2) / (max - min) + 240.0F;
            }

            n5 = max;
        }

        float n6 = (n5 + min) / 2.0F;
        float n7;

        if (max == min) {
            n7 = 0.0F;
        } else {
            float n8 = Math.min(n6, 0.5F);

            if (n8 <= 0.0F) {
                n7 = (max - min) / (max + min);
            } else {
                n7 = (max - min) / (2.0F - max - min);
            }
        }

        return new float[] { n4, n7 * 100.0F, n6 * 100.0F};
    }

    public Color GetColorWithModifiedHue() {
        return ColorRainbowWithDefaultAlpha((this.m_HSB[0] + 180.0F) % 360.0F, this.m_HSB[1], this.m_HSB[2]);
    }

    public static Color GetRainbowColorFromArray(float[] p_HSB, float p_Alpha) {
        return GetRainbowColor(p_HSB[0], p_HSB[1], p_HSB[2], p_Alpha);
    }

    public static Color GetColorWithHSBArray(float[] HSB) {
        return GetRainbowColorFromArray(HSB, 1.0F);
    }

    public static String GenerateMCColorString(String p_String) {
        boolean c = true;
        boolean c2 = true;
        int length = p_String.length();
        char[] array = new char[length];
        int n;
        int i = n = length - 1;
        char[] array2 = array;
        boolean c3 = true;

        for (boolean c4 = true; i >= 0; i = n) {
            int n2 = n;
            char char1 = p_String.charAt(n);

            --n;
            array2[n2] = (char) (char1 ^ 113);
            if (n < 0) {
                break;
            }

            int n3 = n--;

            array2[n3] = (char) (p_String.charAt(n3) ^ 24);
        }

        return new String(array2);
    }

    private static float FutureClientColorCalculation(float n, float n2, float n3) {
        if (n3 < 0.0F) {
            ++n3;
        }

        if (n3 > 1.0F) {
            --n3;
        }

        return 6.0F * n3 < 1.0F ? n + (n2 - n) * 6.0F * n3 : (2.0F * n3 < 1.0F ? n2 : (3.0F * n3 < 2.0F ? n + (n2 - n) * 6.0F * (0.6666667F - n3) : n));
    }

    public static Color ColorRainbowWithDefaultAlpha(float n, float n2, float n3) {
        return GetRainbowColor(n, n2, n3, 1.0F);
    }

    public static Color GetRainbowColor(float p_Hue, float p_Saturation, float p_Lightness, float p_Alpha) {
        if (p_Saturation >= 0.0F && p_Saturation <= 100.0F) {
            if (p_Lightness >= 0.0F && p_Lightness <= 100.0F) {
                if (p_Alpha >= 0.0F && p_Alpha <= 1.0F) {
                    p_Hue = (p_Hue %= 360.0F) / 360.0F;
                    p_Saturation /= 100.0F;
                    p_Lightness /= 100.0F;
                    float n5;

                    if ((double) p_Lightness < 0.0D) {
                        n5 = p_Lightness * (1.0F + p_Saturation);
                    } else {
                        n5 = p_Lightness + p_Saturation - p_Saturation * p_Lightness;
                    }

                    p_Saturation = 2.0F * p_Lightness - n5;
                    p_Lightness = Math.max(0.0F, FutureClientColorCalculation(p_Saturation, n5, p_Hue + 0.33333334F));
                    float max = Math.max(0.0F, FutureClientColorCalculation(p_Saturation, n5, p_Hue));

                    p_Saturation = Math.max(0.0F, FutureClientColorCalculation(p_Saturation, n5, p_Hue - 0.33333334F));
                    p_Lightness = Math.min(p_Lightness, 1.0F);
                    float min = Math.min(max, 1.0F);

                    p_Saturation = Math.min(p_Saturation, 1.0F);
                    return new Color(p_Lightness, min, p_Saturation, p_Alpha);
                } else {
                    throw new IllegalArgumentException("Color nameeter outside of expected range - Alpha");
                }
            } else {
                throw new IllegalArgumentException("Color nameeter outside of expected range - Lightness");
            }
        } else {
            throw new IllegalArgumentException("Color nameeter outside of expected range - Saturation");
        }
    }
}
