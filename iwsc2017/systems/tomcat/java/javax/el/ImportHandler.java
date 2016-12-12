package javax.el;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ImportHandler {
    private List<String> packageNames = new ArrayList<>();
    private Map<String, String> classNames = new ConcurrentHashMap<>();
    private Map<String, Class<?>> clazzes = new ConcurrentHashMap<>();
    private Map<String, Class<?>> statics = new ConcurrentHashMap<>();
    public ImportHandler() {
        importPackage ( "java.lang" );
    }
    public void importStatic ( String name ) throws javax.el.ELException {
        int lastPeriod = name.lastIndexOf ( '.' );
        if ( lastPeriod < 0 ) {
            throw new ELException ( Util.message (
                                        null, "importHandler.invalidStaticName", name ) );
        }
        String className = name.substring ( 0, lastPeriod );
        String fieldOrMethodName = name.substring ( lastPeriod + 1 );
        Class<?> clazz = findClass ( className, true );
        if ( clazz == null ) {
            throw new ELException ( Util.message (
                                        null, "importHandler.invalidClassNameForStatic",
                                        className, name ) );
        }
        boolean found = false;
        for ( Field field : clazz.getFields() ) {
            if ( field.getName().equals ( fieldOrMethodName ) ) {
                int modifiers = field.getModifiers();
                if ( Modifier.isStatic ( modifiers ) &&
                        Modifier.isPublic ( modifiers ) ) {
                    found = true;
                    break;
                }
            }
        }
        if ( !found ) {
            for ( Method method : clazz.getMethods() ) {
                if ( method.getName().equals ( fieldOrMethodName ) ) {
                    int modifiers = method.getModifiers();
                    if ( Modifier.isStatic ( modifiers ) &&
                            Modifier.isPublic ( modifiers ) ) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if ( !found ) {
            throw new ELException ( Util.message ( null,
                                                   "importHandler.staticNotFound", fieldOrMethodName,
                                                   className, name ) );
        }
        Class<?> conflict = statics.get ( fieldOrMethodName );
        if ( conflict != null ) {
            throw new ELException ( Util.message ( null,
                                                   "importHandler.ambiguousStaticImport", name,
                                                   conflict.getName() + '.' +  fieldOrMethodName ) );
        }
        statics.put ( fieldOrMethodName, clazz );
    }
    public void importClass ( String name ) throws javax.el.ELException {
        int lastPeriodIndex = name.lastIndexOf ( '.' );
        if ( lastPeriodIndex < 0 ) {
            throw new ELException ( Util.message (
                                        null, "importHandler.invalidClassName", name ) );
        }
        String unqualifiedName = name.substring ( lastPeriodIndex + 1 );
        String currentName = classNames.putIfAbsent ( unqualifiedName, name );
        if ( currentName != null && !currentName.equals ( name ) ) {
            throw new ELException ( Util.message ( null,
                                                   "importHandler.ambiguousImport", name, currentName ) );
        }
    }
    public void importPackage ( String name ) {
        packageNames.add ( name );
    }
    public java.lang.Class<?> resolveClass ( String name ) {
        if ( name == null || name.contains ( "." ) ) {
            return null;
        }
        Class<?> result = clazzes.get ( name );
        if ( result != null ) {
            if ( NotFound.class.equals ( result ) ) {
                return null;
            } else {
                return result;
            }
        }
        String className = classNames.get ( name );
        if ( className != null ) {
            Class<?> clazz = findClass ( className, true );
            if ( clazz != null ) {
                clazzes.put ( className, clazz );
                return clazz;
            }
        }
        for ( String p : packageNames ) {
            className = p + '.' + name;
            Class<?> clazz = findClass ( className, false );
            if ( clazz != null ) {
                if ( result != null ) {
                    throw new ELException ( Util.message ( null,
                                                           "importHandler.ambiguousImport", className,
                                                           result.getName() ) );
                }
                result = clazz;
            }
        }
        if ( result == null ) {
            clazzes.put ( name, NotFound.class );
        } else {
            clazzes.put ( name, result );
        }
        return result;
    }
    public java.lang.Class<?> resolveStatic ( String name ) {
        return statics.get ( name );
    }
    private Class<?> findClass ( String name, boolean throwException ) {
        Class<?> clazz;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = name.replace ( '.', '/' ) + ".class";
        try {
            if ( cl.getResource ( path ) == null ) {
                return null;
            }
        } catch ( ClassCircularityError cce ) {
        }
        try {
            clazz = cl.loadClass ( name );
        } catch ( ClassNotFoundException e ) {
            return null;
        }
        int modifiers = clazz.getModifiers();
        if ( !Modifier.isPublic ( modifiers ) || Modifier.isAbstract ( modifiers ) ||
                Modifier.isInterface ( modifiers ) ) {
            if ( throwException ) {
                throw new ELException ( Util.message (
                                            null, "importHandler.invalidClass", name ) );
            } else {
                return null;
            }
        }
        return clazz;
    }
    private static class NotFound {
    }
}
