package org.spongepowered.asm.util.perf;

import com.google.common.base.Joiner;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.spongepowered.asm.util.PrettyPrinter;

public final class Profiler {

    public static final int ROOT = 1;
    public static final int FINE = 2;
    private final Map sections = new TreeMap();
    private final List phases = new ArrayList();
    private final Deque stack = new LinkedList();
    private boolean active;

    public Profiler() {
        this.phases.add("Initial");
    }

    public void setActive(boolean active) {
        if (!this.active && active || !active) {
            this.reset();
        }

        this.active = active;
    }

    public void reset() {
        Iterator iterator = this.sections.values().iterator();

        while (iterator.hasNext()) {
            Profiler.Section section = (Profiler.Section) iterator.next();

            section.invalidate();
        }

        this.sections.clear();
        this.phases.clear();
        this.phases.add("Initial");
        this.stack.clear();
    }

    public Profiler.Section get(String name) {
        Object section = (Profiler.Section) this.sections.get(name);

        if (section == null) {
            section = this.active ? new Profiler.LiveSection(name, this.phases.size() - 1) : new Profiler.Section(name);
            this.sections.put(name, section);
        }

        return (Profiler.Section) section;
    }

    private Profiler.Section getSubSection(String name, String baseName, Profiler.Section root) {
        Object section = (Profiler.Section) this.sections.get(name);

        if (section == null) {
            section = new Profiler.SubSection(name, this.phases.size() - 1, baseName, root);
            this.sections.put(name, section);
        }

        return (Profiler.Section) section;
    }

    boolean isHead(Profiler.Section section) {
        return this.stack.peek() == section;
    }

    public Profiler.Section begin(String... path) {
        return this.begin(0, path);
    }

    public Profiler.Section begin(int flags, String... path) {
        return this.begin(flags, Joiner.on('.').join(path));
    }

    public Profiler.Section begin(String name) {
        return this.begin(0, name);
    }

    public Profiler.Section begin(int flags, String name) {
        boolean root = (flags & 1) != 0;
        boolean fine = (flags & 2) != 0;
        String path = name;
        Profiler.Section head = (Profiler.Section) this.stack.peek();

        if (head != null) {
            path = head.getName() + (root ? " -> " : ".") + name;
            if (head.isRoot() && !root) {
                int section = head.getName().lastIndexOf(" -> ");

                name = (section > -1 ? head.getName().substring(section + 4) : head.getName()) + "." + name;
                root = true;
            }
        }

        Profiler.Section section1 = this.get(root ? name : path);

        if (root && head != null && this.active) {
            section1 = this.getSubSection(path, head.getName(), section1);
        }

        section1.setFine(fine).setRoot(root);
        this.stack.push(section1);
        return section1.start();
    }

    void end(Profiler.Section section) {
        try {
            Profiler.Section ex = (Profiler.Section) this.stack.pop();

            for (Profiler.Section next = ex; next != section; next = (Profiler.Section) this.stack.pop()) {
                if (next == null && this.active) {
                    if (ex == null) {
                        throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
                    }

                    throw new IllegalStateException("Attempted to pop " + section + " which was not in the stack, head was " + ex);
                }
            }
        } catch (NoSuchElementException nosuchelementexception) {
            if (this.active) {
                throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
            }
        }

    }

    public void mark(String phase) {
        long currentPhaseTime = 0L;

        Iterator size;
        Profiler.Section section;

        for (size = this.sections.values().iterator(); size.hasNext(); currentPhaseTime += section.getTime()) {
            section = (Profiler.Section) size.next();
        }

        if (currentPhaseTime == 0L) {
            int size1 = this.phases.size();

            this.phases.set(size1 - 1, phase);
        } else {
            this.phases.add(phase);
            size = this.sections.values().iterator();

            while (size.hasNext()) {
                section = (Profiler.Section) size.next();
                section.mark();
            }

        }
    }

    public Collection getSections() {
        return Collections.unmodifiableCollection(this.sections.values());
    }

    public PrettyPrinter printer(boolean includeFine, boolean group) {
        PrettyPrinter printer = new PrettyPrinter();
        int colCount = this.phases.size() + 4;
        int[] columns = new int[] { 0, 1, 2, colCount - 2, colCount - 1};
        Object[] headers = new Object[colCount * 2];
        int col = 0;

        for (int section = 0; col < colCount; section = col * 2) {
            headers[section + 1] = PrettyPrinter.Alignment.RIGHT;
            if (col == columns[0]) {
                headers[section] = (group ? "" : "  ") + "Section";
                headers[section + 1] = PrettyPrinter.Alignment.LEFT;
            } else if (col == columns[1]) {
                headers[section] = "    TOTAL";
            } else if (col == columns[3]) {
                headers[section] = "    Count";
            } else if (col == columns[4]) {
                headers[section] = "Avg. ";
            } else if (col - columns[2] < this.phases.size()) {
                headers[section] = this.phases.get(col - columns[2]);
            } else {
                headers[section] = "";
            }

            ++col;
        }

        printer.table(headers).th().hr().add();
        Iterator iterator = this.sections.values().iterator();

        while (iterator.hasNext()) {
            Profiler.Section profiler_section = (Profiler.Section) iterator.next();

            if ((!profiler_section.isFine() || includeFine) && (!group || profiler_section.getDelegate() == profiler_section)) {
                this.printSectionRow(printer, colCount, columns, profiler_section, group);
                if (group) {
                    Iterator iterator1 = this.sections.values().iterator();

                    while (iterator1.hasNext()) {
                        Profiler.Section subSection = (Profiler.Section) iterator1.next();
                        Profiler.Section delegate = subSection.getDelegate();

                        if ((!subSection.isFine() || includeFine) && delegate == profiler_section && delegate != subSection) {
                            this.printSectionRow(printer, colCount, columns, subSection, group);
                        }
                    }
                }
            }
        }

        return printer.add();
    }

    private void printSectionRow(PrettyPrinter printer, int colCount, int[] columns, Profiler.Section section, boolean group) {
        boolean isDelegate = section.getDelegate() != section;
        Object[] values = new Object[colCount];
        int col = 1;

        if (group) {
            values[0] = isDelegate ? "  > " + section.getBaseName() : section.getName();
        } else {
            values[0] = (isDelegate ? "+ " : "  ") + section.getName();
        }

        long[] times = section.getTimes();
        long[] i = times;
        int i = times.length;

        for (int j = 0; j < i; ++j) {
            long time = i[j];

            if (col == columns[1]) {
                values[col++] = section.getTotalTime() + " ms";
            }

            if (col >= columns[2] && col < values.length) {
                values[col++] = time + " ms";
            }
        }

        values[columns[3]] = Integer.valueOf(section.getTotalCount());
        values[columns[4]] = (new DecimalFormat("   ###0.000 ms")).format(section.getTotalAverageTime());

        for (int k = 0; k < values.length; ++k) {
            if (values[k] == null) {
                values[k] = "-";
            }
        }

        printer.tr(values);
    }

    class SubSection extends Profiler.LiveSection {

        private final String baseName;
        private final Profiler.Section root;

        SubSection(String name, int cursor, String baseName, Profiler.Section root) {
            super(name, cursor);
            this.baseName = baseName;
            this.root = root;
        }

        Profiler.Section invalidate() {
            this.root.invalidate();
            return super.invalidate();
        }

        public String getBaseName() {
            return this.baseName;
        }

        public void setInfo(String info) {
            this.root.setInfo(info);
            super.setInfo(info);
        }

        Profiler.Section getDelegate() {
            return this.root;
        }

        Profiler.Section start() {
            this.root.start();
            return super.start();
        }

        public Profiler.Section end() {
            this.root.stop();
            return super.end();
        }

        public Profiler.Section next(String name) {
            super.stop();
            return this.root.next(name);
        }
    }

    class LiveSection extends Profiler.Section {

        private int cursor = 0;
        private long[] times = new long[0];
        private long start = 0L;
        private long time;
        private long markedTime;
        private int count;
        private int markedCount;

        LiveSection(String name, int cursor) {
            super(name);
            this.cursor = cursor;
        }

        Profiler.Section start() {
            this.start = System.currentTimeMillis();
            return this;
        }

        protected Profiler.Section stop() {
            if (this.start > 0L) {
                this.time += System.currentTimeMillis() - this.start;
            }

            this.start = 0L;
            ++this.count;
            return this;
        }

        public Profiler.Section end() {
            this.stop();
            if (!this.invalidated) {
                Profiler.this.end(this);
            }

            return this;
        }

        void mark() {
            if (this.cursor >= this.times.length) {
                this.times = Arrays.copyOf(this.times, this.cursor + 4);
            }

            this.times[this.cursor] = this.time;
            this.markedTime += this.time;
            this.markedCount += this.count;
            this.time = 0L;
            this.count = 0;
            ++this.cursor;
        }

        public long getTime() {
            return this.time;
        }

        public long getTotalTime() {
            return this.time + this.markedTime;
        }

        public double getSeconds() {
            return (double) this.time * 0.001D;
        }

        public double getTotalSeconds() {
            return (double) (this.time + this.markedTime) * 0.001D;
        }

        public long[] getTimes() {
            long[] times = new long[this.cursor + 1];

            System.arraycopy(this.times, 0, times, 0, Math.min(this.times.length, this.cursor));
            times[this.cursor] = this.time;
            return times;
        }

        public int getCount() {
            return this.count;
        }

        public int getTotalCount() {
            return this.count + this.markedCount;
        }

        public double getAverageTime() {
            return this.count > 0 ? (double) this.time / (double) this.count : 0.0D;
        }

        public double getTotalAverageTime() {
            return this.count > 0 ? (double) (this.time + this.markedTime) / (double) (this.count + this.markedCount) : 0.0D;
        }
    }

    public class Section {

        static final String SEPARATOR_ROOT = " -> ";
        static final String SEPARATOR_CHILD = ".";
        private final String name;
        private boolean root;
        private boolean fine;
        protected boolean invalidated;
        private String info;

        Section(String name) {
            this.name = name;
            this.info = name;
        }

        Profiler.Section getDelegate() {
            return this;
        }

        Profiler.Section invalidate() {
            this.invalidated = true;
            return this;
        }

        Profiler.Section setRoot(boolean root) {
            this.root = root;
            return this;
        }

        public boolean isRoot() {
            return this.root;
        }

        Profiler.Section setFine(boolean fine) {
            this.fine = fine;
            return this;
        }

        public boolean isFine() {
            return this.fine;
        }

        public String getName() {
            return this.name;
        }

        public String getBaseName() {
            return this.name;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getInfo() {
            return this.info;
        }

        Profiler.Section start() {
            return this;
        }

        protected Profiler.Section stop() {
            return this;
        }

        public Profiler.Section end() {
            if (!this.invalidated) {
                Profiler.this.end(this);
            }

            return this;
        }

        public Profiler.Section next(String name) {
            this.end();
            return Profiler.this.begin(name);
        }

        void mark() {}

        public long getTime() {
            return 0L;
        }

        public long getTotalTime() {
            return 0L;
        }

        public double getSeconds() {
            return 0.0D;
        }

        public double getTotalSeconds() {
            return 0.0D;
        }

        public long[] getTimes() {
            return new long[1];
        }

        public int getCount() {
            return 0;
        }

        public int getTotalCount() {
            return 0;
        }

        public double getAverageTime() {
            return 0.0D;
        }

        public double getTotalAverageTime() {
            return 0.0D;
        }

        public final String toString() {
            return this.name;
        }
    }
}
