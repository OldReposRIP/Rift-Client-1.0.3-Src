package org.yaml.snakeyaml.external.com.google.gdata.util.common.base;

import java.io.IOException;

public abstract class UnicodeEscaper implements Escaper {

    private static final int DEST_PAD = 32;
    private static final ThreadLocal DEST_TL = new ThreadLocal() {
        protected char[] initialValue() {
            return new char[1024];
        }
    };

    protected abstract char[] escape(int i);

    protected int nextEscapeIndex(CharSequence csq, int start, int end) {
        int index;
        int cp;

        for (index = start; index < end; index += Character.isSupplementaryCodePoint(cp) ? 2 : 1) {
            cp = codePointAt(csq, index, end);
            if (cp < 0 || this.escape(cp) != null) {
                break;
            }
        }

        return index;
    }

    public String escape(String string) {
        int end = string.length();
        int index = this.nextEscapeIndex(string, 0, end);

        return index == end ? string : this.escapeSlow(string, index);
    }

    protected final String escapeSlow(String s, int index) {
        int end = s.length();
        char[] dest = (char[]) UnicodeEscaper.DEST_TL.get();
        int destIndex = 0;

        int unescapedChunkStart;
        int charsSkipped;

        for (unescapedChunkStart = 0; index < end; index = this.nextEscapeIndex(s, unescapedChunkStart, end)) {
            charsSkipped = codePointAt(s, index, end);
            if (charsSkipped < 0) {
                throw new IllegalArgumentException("Trailing high surrogate at end of input");
            }

            char[] endIndex = this.escape(charsSkipped);

            if (endIndex != null) {
                int charsSkipped1 = index - unescapedChunkStart;
                int sizeNeeded = destIndex + charsSkipped1 + endIndex.length;

                if (dest.length < sizeNeeded) {
                    int destLength = sizeNeeded + (end - index) + 32;

                    dest = growBuffer(dest, destIndex, destLength);
                }

                if (charsSkipped1 > 0) {
                    s.getChars(unescapedChunkStart, index, dest, destIndex);
                    destIndex += charsSkipped1;
                }

                if (endIndex.length > 0) {
                    System.arraycopy(endIndex, 0, dest, destIndex, endIndex.length);
                    destIndex += endIndex.length;
                }
            }

            unescapedChunkStart = index + (Character.isSupplementaryCodePoint(charsSkipped) ? 2 : 1);
        }

        charsSkipped = end - unescapedChunkStart;
        if (charsSkipped > 0) {
            int endIndex1 = destIndex + charsSkipped;

            if (dest.length < endIndex1) {
                dest = growBuffer(dest, destIndex, endIndex1);
            }

            s.getChars(unescapedChunkStart, end, dest, destIndex);
            destIndex = endIndex1;
        }

        return new String(dest, 0, destIndex);
    }

    public Appendable escape(final Appendable out) {
        assert out != null;

        return new Appendable() {
            int pendingHighSurrogate = -1;
            char[] decodedChars = new char[2];

            public Appendable append(CharSequence csq) throws IOException {
                return this.append(csq, 0, csq.length());
            }

            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                int index = start;

                if (start < end) {
                    int unescapedChunkStart = start;
                    char[] escaped;

                    if (this.pendingHighSurrogate != -1) {
                        index = start + 1;
                        char cp = csq.charAt(start);

                        if (!Character.isLowSurrogate(cp)) {
                            throw new IllegalArgumentException("Expected low surrogate character but got " + cp);
                        }

                        escaped = UnicodeEscaper.this.escape(Character.toCodePoint((char) this.pendingHighSurrogate, cp));
                        if (escaped != null) {
                            this.outputChars(escaped, escaped.length);
                            unescapedChunkStart = start + 1;
                        } else {
                            out.append((char) this.pendingHighSurrogate);
                        }

                        this.pendingHighSurrogate = -1;
                    }

                    while (true) {
                        index = UnicodeEscaper.this.nextEscapeIndex(csq, index, end);
                        if (index > unescapedChunkStart) {
                            out.append(csq, unescapedChunkStart, index);
                        }

                        if (index == end) {
                            break;
                        }

                        int cp1 = UnicodeEscaper.codePointAt(csq, index, end);

                        if (cp1 < 0) {
                            this.pendingHighSurrogate = -cp1;
                            break;
                        }

                        escaped = UnicodeEscaper.this.escape(cp1);
                        if (escaped != null) {
                            this.outputChars(escaped, escaped.length);
                        } else {
                            int len = Character.toChars(cp1, this.decodedChars, 0);

                            this.outputChars(this.decodedChars, len);
                        }

                        index += Character.isSupplementaryCodePoint(cp1) ? 2 : 1;
                        unescapedChunkStart = index;
                    }
                }

                return this;
            }

            public Appendable append(char c) throws IOException {
                char[] escaped;

                if (this.pendingHighSurrogate != -1) {
                    if (!Character.isLowSurrogate(c)) {
                        throw new IllegalArgumentException("Expected low surrogate character but got \'" + c + "\' with value " + c);
                    }

                    escaped = UnicodeEscaper.this.escape(Character.toCodePoint((char) this.pendingHighSurrogate, c));
                    if (escaped != null) {
                        this.outputChars(escaped, escaped.length);
                    } else {
                        out.append((char) this.pendingHighSurrogate);
                        out.append(c);
                    }

                    this.pendingHighSurrogate = -1;
                } else if (Character.isHighSurrogate(c)) {
                    this.pendingHighSurrogate = c;
                } else {
                    if (Character.isLowSurrogate(c)) {
                        throw new IllegalArgumentException("Unexpected low surrogate character \'" + c + "\' with value " + c);
                    }

                    escaped = UnicodeEscaper.this.escape(c);
                    if (escaped != null) {
                        this.outputChars(escaped, escaped.length);
                    } else {
                        out.append(c);
                    }
                }

                return this;
            }

            private void outputChars(char[] chars, int len) throws IOException {
                for (int n = 0; n < len; ++n) {
                    out.append(chars[n]);
                }

            }
        };
    }

    protected static final int codePointAt(CharSequence seq, int index, int end) {
        if (index < end) {
            char c1 = seq.charAt(index++);

            if (c1 >= '\ud800' && c1 <= '\udfff') {
                if (c1 <= '\udbff') {
                    if (index == end) {
                        return -c1;
                    } else {
                        char c2 = seq.charAt(index);

                        if (Character.isLowSurrogate(c2)) {
                            return Character.toCodePoint(c1, c2);
                        } else {
                            throw new IllegalArgumentException("Expected low surrogate but got char \'" + c2 + "\' with value " + c2 + " at index " + index);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected low surrogate character \'" + c1 + "\' with value " + c1 + " at index " + (index - 1));
                }
            } else {
                return c1;
            }
        } else {
            throw new IndexOutOfBoundsException("Index exceeds specified range");
        }
    }

    private static final char[] growBuffer(char[] dest, int index, int size) {
        char[] copy = new char[size];

        if (index > 0) {
            System.arraycopy(dest, 0, copy, 0, index);
        }

        return copy;
    }
}
