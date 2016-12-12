package org.junit.experimental.categories;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
private static class IncludesAny extends Categories.CategoryFilter {
    public IncludesAny ( final List<Class<?>> categories ) {
        this ( new HashSet<Class<?>> ( categories ) );
    }
    public IncludesAny ( final Set<Class<?>> categories ) {
        super ( true, categories, true, null );
    }
    public String describe() {
        return "includes " + super.describe();
    }
}
