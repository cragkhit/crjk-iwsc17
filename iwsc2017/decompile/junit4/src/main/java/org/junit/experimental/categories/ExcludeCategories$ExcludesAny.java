package org.junit.experimental.categories;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
private static class ExcludesAny extends Categories.CategoryFilter {
    public ExcludesAny ( final List<Class<?>> categories ) {
        this ( new HashSet<Class<?>> ( categories ) );
    }
    public ExcludesAny ( final Set<Class<?>> categories ) {
        super ( true, null, true, categories );
    }
    public String describe() {
        return "excludes " + super.describe();
    }
}
