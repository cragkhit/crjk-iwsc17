package javax.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
public class StaticFieldELResolver extends ELResolver {
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( ! ( base instanceof ELClass ) || ! ( property instanceof String ) ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final Class<?> clazz = ( ( ELClass ) base ).getKlass();
        final String name = ( String ) property;
        Exception exception = null;
        try {
            final Field field = clazz.getField ( name );
            final int modifiers = field.getModifiers();
            if ( Modifier.isStatic ( modifiers ) && Modifier.isPublic ( modifiers ) ) {
                return field.get ( null );
            }
        } catch ( IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e ) {
            exception = e;
        }
        final String msg = Util.message ( context, "staticFieldELResolver.notFound", name, clazz.getName() );
        if ( exception == null ) {
            throw new PropertyNotFoundException ( msg );
        }
        throw new PropertyNotFoundException ( msg, exception );
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ELClass && property instanceof String ) {
            final Class<?> clazz = ( ( ELClass ) base ).getKlass();
            final String name = ( String ) property;
            throw new PropertyNotWritableException ( Util.message ( context, "staticFieldELResolver.notWriteable", name, clazz.getName() ) );
        }
    }
    @Override
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        Objects.requireNonNull ( context );
        if ( ! ( base instanceof ELClass ) || ! ( method instanceof String ) ) {
            return null;
        }
        context.setPropertyResolved ( base, method );
        final Class<?> clazz = ( ( ELClass ) base ).getKlass();
        final String methodName = ( String ) method;
        if ( "<init>".equals ( methodName ) ) {
            final Constructor<?> match = Util.findConstructor ( clazz, paramTypes, params );
            final Object[] parameters = Util.buildParameters ( match.getParameterTypes(), match.isVarArgs(), params );
            Object result = null;
            try {
                result = match.newInstance ( parameters );
            } catch ( IllegalArgumentException | IllegalAccessException | InstantiationException e ) {
                throw new ELException ( e );
            } catch ( InvocationTargetException e2 ) {
                final Throwable cause = e2.getCause();
                Util.handleThrowable ( cause );
                throw new ELException ( cause );
            }
            return result;
        }
        final Method match2 = Util.findMethod ( clazz, methodName, paramTypes, params );
        final int modifiers = match2.getModifiers();
        if ( !Modifier.isStatic ( modifiers ) ) {
            throw new MethodNotFoundException ( Util.message ( context, "staticFieldELResolver.methodNotFound", methodName, clazz.getName() ) );
        }
        final Object[] parameters2 = Util.buildParameters ( match2.getParameterTypes(), match2.isVarArgs(), params );
        Object result2 = null;
        try {
            result2 = match2.invoke ( null, parameters2 );
        } catch ( IllegalArgumentException | IllegalAccessException e3 ) {
            throw new ELException ( e3 );
        } catch ( InvocationTargetException e4 ) {
            final Throwable cause2 = e4.getCause();
            Util.handleThrowable ( cause2 );
            throw new ELException ( cause2 );
        }
        return result2;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( ! ( base instanceof ELClass ) || ! ( property instanceof String ) ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final Class<?> clazz = ( ( ELClass ) base ).getKlass();
        final String name = ( String ) property;
        Exception exception = null;
        try {
            final Field field = clazz.getField ( name );
            final int modifiers = field.getModifiers();
            if ( Modifier.isStatic ( modifiers ) && Modifier.isPublic ( modifiers ) ) {
                return field.getType();
            }
        } catch ( IllegalArgumentException | NoSuchFieldException | SecurityException e ) {
            exception = e;
        }
        final String msg = Util.message ( context, "staticFieldELResolver.notFound", name, clazz.getName() );
        if ( exception == null ) {
            throw new PropertyNotFoundException ( msg );
        }
        throw new PropertyNotFoundException ( msg, exception );
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ELClass && property instanceof String ) {
            context.setPropertyResolved ( base, property );
        }
        return true;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        return String.class;
    }
}
