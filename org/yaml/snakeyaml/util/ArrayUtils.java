package org.yaml.snakeyaml.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {

    public static List toUnmodifiableList(Object... elements) {
        return (List) (elements.length == 0 ? Collections.emptyList() : new ArrayUtils.UnmodifiableArrayList(elements));
    }

    public static List toUnmodifiableCompositeList(Object[] array1, Object[] array2) {
        Object result;

        if (array1.length == 0) {
            result = toUnmodifiableList(array2);
        } else if (array2.length == 0) {
            result = toUnmodifiableList(array1);
        } else {
            result = new ArrayUtils.CompositeUnmodifiableArrayList(array1, array2);
        }

        return (List) result;
    }

    private static class CompositeUnmodifiableArrayList extends AbstractList {

        private final Object[] array1;
        private final Object[] array2;

        CompositeUnmodifiableArrayList(Object[] array1, Object[] array2) {
            this.array1 = array1;
            this.array2 = array2;
        }

        public Object get(int index) {
            Object element;

            if (index < this.array1.length) {
                element = this.array1[index];
            } else {
                if (index - this.array1.length >= this.array2.length) {
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
                }

                element = this.array2[index - this.array1.length];
            }

            return element;
        }

        public int size() {
            return this.array1.length + this.array2.length;
        }
    }

    private static class UnmodifiableArrayList extends AbstractList {

        private final Object[] array;

        UnmodifiableArrayList(Object[] array) {
            this.array = array;
        }

        public Object get(int index) {
            if (index >= this.array.length) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size());
            } else {
                return this.array[index];
            }
        }

        public int size() {
            return this.array.length;
        }
    }
}
