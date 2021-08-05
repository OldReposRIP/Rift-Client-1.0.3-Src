package org.yaml.snakeyaml.external.com.google.gdata.util.common.base;

public class PercentEscaper extends UnicodeEscaper {

    public static final String SAFECHARS_URLENCODER = "-_.*";
    public static final String SAFEPATHCHARS_URLENCODER = "-_.!~*\'()@:$&,;=";
    public static final String SAFEQUERYSTRINGCHARS_URLENCODER = "-_.!~*\'()@:$,;/?:";
    private static final char[] URI_ESCAPED_SPACE = new char[] { '+'};
    private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private final boolean plusForSpace;
    private final boolean[] safeOctets;

    public PercentEscaper(String safeChars, boolean plusForSpace) {
        if (safeChars.matches(".*[0-9A-Za-z].*")) {
            throw new IllegalArgumentException("Alphanumeric characters are always \'safe\' and should not be explicitly specified");
        } else if (plusForSpace && safeChars.contains(" ")) {
            throw new IllegalArgumentException("plusForSpace cannot be specified when space is a \'safe\' character");
        } else if (safeChars.contains("%")) {
            throw new IllegalArgumentException("The \'%\' character cannot be specified as \'safe\'");
        } else {
            this.plusForSpace = plusForSpace;
            this.safeOctets = createSafeOctets(safeChars);
        }
    }

    private static boolean[] createSafeOctets(String safeChars) {
        int maxChar = 122;
        char[] safeCharArray = safeChars.toCharArray();
        char[] octets = safeCharArray;
        int c = safeCharArray.length;

        int i;

        for (i = 0; i < c; ++i) {
            char c1 = octets[i];

            maxChar = Math.max(c1, maxChar);
        }

        boolean[] aboolean = new boolean[maxChar + 1];

        for (c = 48; c <= 57; ++c) {
            aboolean[c] = true;
        }

        for (c = 65; c <= 90; ++c) {
            aboolean[c] = true;
        }

        for (c = 97; c <= 122; ++c) {
            aboolean[c] = true;
        }

        char[] achar = safeCharArray;

        i = safeCharArray.length;

        for (int j = 0; j < i; ++j) {
            char c2 = achar[j];

            aboolean[c2] = true;
        }

        return aboolean;
    }

    protected int nextEscapeIndex(CharSequence csq, int index, int end) {
        while (true) {
            if (index < end) {
                char c = csq.charAt(index);

                if (c < this.safeOctets.length && this.safeOctets[c]) {
                    ++index;
                    continue;
                }
            }

            return index;
        }
    }

    public String escape(String s) {
        int slen = s.length();

        for (int index = 0; index < slen; ++index) {
            char c = s.charAt(index);

            if (c >= this.safeOctets.length || !this.safeOctets[c]) {
                return this.escapeSlow(s, index);
            }
        }

        return s;
    }

    protected char[] escape(int cp) {
        if (cp < this.safeOctets.length && this.safeOctets[cp]) {
            return null;
        } else if (cp == 32 && this.plusForSpace) {
            return PercentEscaper.URI_ESCAPED_SPACE;
        } else {
            char[] dest;

            if (cp <= 127) {
                dest = new char[] { '%', PercentEscaper.UPPER_HEX_DIGITS[cp >>> 4], PercentEscaper.UPPER_HEX_DIGITS[cp & 15]};
                return dest;
            } else if (cp <= 2047) {
                dest = new char[] { '%', '\u0000', '\u0000', '%', '\u0000', PercentEscaper.UPPER_HEX_DIGITS[cp & 15]};
                cp >>>= 4;
                dest[4] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[1] = PercentEscaper.UPPER_HEX_DIGITS[12 | cp];
                return dest;
            } else if (cp <= '\uffff') {
                dest = new char[9];
                dest[0] = 37;
                dest[1] = 69;
                dest[3] = 37;
                dest[6] = 37;
                dest[8] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[7] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[5] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[4] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp];
                return dest;
            } else if (cp <= 1114111) {
                dest = new char[12];
                dest[0] = 37;
                dest[1] = 70;
                dest[3] = 37;
                dest[6] = 37;
                dest[9] = 37;
                dest[11] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[10] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[8] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[7] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[5] = PercentEscaper.UPPER_HEX_DIGITS[cp & 15];
                cp >>>= 4;
                dest[4] = PercentEscaper.UPPER_HEX_DIGITS[8 | cp & 3];
                cp >>>= 2;
                dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp & 7];
                return dest;
            } else {
                throw new IllegalArgumentException("Invalid unicode character value " + cp);
            }
        }
    }
}
