package javax.el;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
private abstract static class Wrapper {
    public static List<Wrapper> wrap ( final Method[] methods, final String name ) {
        final List<Wrapper> result = new ArrayList<Wrapper>();
        for ( final Method method : methods ) {
            if ( method.getName().equals ( name ) ) {
                result.add ( new MethodWrapper ( method ) );
            }
        }
        return result;
    }
    public static List<Wrapper> wrap ( final Constructor<?>[] constructors ) {
        final List<Wrapper> result = new ArrayList<Wrapper>();
        for ( final Constructor<?> constructor : constructors ) {
            result.add ( new ConstructorWrapper ( constructor ) );
        }
        return result;
    }
    public abstract Object unWrap();
    public abstract Class<?>[] getParameterTypes();
    public abstract boolean isVarArgs();
    public abstract boolean isBridge();
}
