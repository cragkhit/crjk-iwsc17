package javax.el;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
public class ImportHandler {
    private List<String> packageNames;
    private Map<String, String> classNames;
    private Map<String, Class<?>> clazzes;
    private Map<String, Class<?>> statics;
    public ImportHandler() {
        this.packageNames = new ArrayList<String>();
        this.classNames = new ConcurrentHashMap<String, String>();
        this.clazzes = new ConcurrentHashMap<String, Class<?>>();
        this.statics = new ConcurrentHashMap<String, Class<?>>();
        this.importPackage ( "java.lang" );
    }
    public void importStatic ( final String name ) throws ELException {
        final int lastPeriod = name.lastIndexOf ( 46 );
        if ( lastPeriod < 0 ) {
            throw new ELException ( Util.message ( null, "importHandler.invalidStaticName", name ) );
        }
        final String className = name.substring ( 0, lastPeriod );
        final String fieldOrMethodName = name.substring ( lastPeriod + 1 );
        final Class<?> clazz = this.findClass ( className, true );
        if ( clazz == null ) {
            throw new ELException ( Util.message ( null, "importHandler.invalidClassNameForStatic", className, name ) );
        }
        boolean found = false;
        for ( final Field field : clazz.getFields() ) {
            if ( field.getName().equals ( fieldOrMethodName ) ) {
                final int modifiers = field.getModifiers();
                if ( Modifier.isStatic ( modifiers ) && Modifier.isPublic ( modifiers ) ) {
                    found = true;
                    break;
                }
            }
        }
        if ( !found ) {
            for ( final Method method : clazz.getMethods() ) {
                if ( method.getName().equals ( fieldOrMethodName ) ) {
                    final int modifiers = method.getModifiers();
                    if ( Modifier.isStatic ( modifiers ) && Modifier.isPublic ( modifiers ) ) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if ( !found ) {
            throw new ELException ( Util.message ( null, "importHandler.staticNotFound", fieldOrMethodName, className, name ) );
        }
        final Class<?> conflict = this.statics.get ( fieldOrMethodName );
        if ( conflict != null ) {
            throw new ELException ( Util.message ( null, "importHandler.ambiguousStaticImport", name, conflict.getName() + '.' + fieldOrMethodName ) );
        }
        this.statics.put ( fieldOrMethodName, clazz );
    }
    public void importClass ( final String name ) throws ELException {
        final int lastPeriodIndex = name.lastIndexOf ( 46 );
        if ( lastPeriodIndex < 0 ) {
            throw new ELException ( Util.message ( null, "importHandler.invalidClassName", name ) );
        }
        final String unqualifiedName = name.substring ( lastPeriodIndex + 1 );
        final String currentName = this.classNames.putIfAbsent ( unqualifiedName, name );
        if ( currentName != null && !currentName.equals ( name ) ) {
            throw new ELException ( Util.message ( null, "importHandler.ambiguousImport", name, currentName ) );
        }
    }
    public void importPackage ( final String name ) {
        this.packageNames.add ( name );
    }
    public Class<?> resolveClass ( final String name ) {
        if ( name == null || name.contains ( "." ) ) {
            return null;
        }
        Class<?> result = this.clazzes.get ( name );
        if ( result == null ) {
            String className = this.classNames.get ( name );
            if ( className != null ) {
                final Class<?> clazz = this.findClass ( className, true );
                if ( clazz != null ) {
                    this.clazzes.put ( className, clazz );
                    return clazz;
                }
            }
            for ( final String p : this.packageNames ) {
                className = p + '.' + name;
                final Class<?> clazz2 = this.findClass ( className, false );
                if ( clazz2 != null ) {
                    if ( result != null ) {
                        throw new ELException ( Util.message ( null, "importHandler.ambiguousImport", className, result.getName() ) );
                    }
                    result = clazz2;
                }
            }
            if ( result == null ) {
                this.clazzes.put ( name, NotFound.class );
            } else {
                this.clazzes.put ( name, result );
            }
            return result;
        }
        if ( NotFound.class.equals ( result ) ) {
            return null;
        }
        return result;
    }
    public Class<?> resolveStatic ( final String name ) {
        return this.statics.get ( name );
    }
    private Class<?> findClass ( final String name, final boolean throwException ) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final String path = name.replace ( '.', '/' ) + ".class";
        try {
            if ( cl.getResource ( path ) == null ) {
                return null;
            }
        } catch ( ClassCircularityError classCircularityError ) {}
        Class<?> clazz;
        try {
            clazz = cl.loadClass ( name );
        } catch ( ClassNotFoundException e ) {
            return null;
        }
        final int modifiers = clazz.getModifiers();
        if ( Modifier.isPublic ( modifiers ) && !Modifier.isAbstract ( modifiers ) && !Modifier.isInterface ( modifiers ) ) {
            return clazz;
        }
        if ( throwException ) {
            throw new ELException ( Util.message ( null, "importHandler.invalidClass", name ) );
        }
        return null;
    }
    private static class NotFound {
    }
}
