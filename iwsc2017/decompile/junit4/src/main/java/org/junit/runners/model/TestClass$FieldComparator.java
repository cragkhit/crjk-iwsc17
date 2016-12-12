package org.junit.runners.model;
import java.lang.reflect.Field;
import java.util.Comparator;
private static class FieldComparator implements Comparator<Field> {
    public int compare ( final Field left, final Field right ) {
        return left.getName().compareTo ( right.getName() );
    }
}
