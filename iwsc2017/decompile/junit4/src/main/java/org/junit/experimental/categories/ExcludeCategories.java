package org.junit.experimental.categories;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;
import java.util.List;
public final class ExcludeCategories extends CategoryFilterFactory {
    protected Filter createFilter ( final List<Class<?>> categories ) {
        return new ExcludesAny ( categories );
    }
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
}
