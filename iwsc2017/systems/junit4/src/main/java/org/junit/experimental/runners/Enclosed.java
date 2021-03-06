package org.junit.experimental.runners;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;
public class Enclosed extends Suite {
    public Enclosed ( Class<?> klass, RunnerBuilder builder ) throws Throwable {
        super ( builder, klass, filterAbstractClasses ( klass.getClasses() ) );
    }
    private static Class<?>[] filterAbstractClasses ( final Class<?>[] classes ) {
        final List<Class<?>> filteredList = new ArrayList<Class<?>> ( classes.length );
        for ( final Class<?> clazz : classes ) {
            if ( !Modifier.isAbstract ( clazz.getModifiers() ) ) {
                filteredList.add ( clazz );
            }
        }
        return filteredList.toArray ( new Class<?>[filteredList.size()] );
    }
}
