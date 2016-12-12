package org.apache.jasper.runtime;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditor;
import javax.servlet.jsp.PageContext;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.lang.reflect.Method;
import org.apache.jasper.compiler.Localizer;
import java.beans.Introspector;
import java.util.Enumeration;
import org.apache.jasper.JasperException;
import java.io.File;
import javax.servlet.ServletRequest;
public class JspRuntimeLibrary {
    public static Throwable getThrowable ( final ServletRequest request ) {
        Throwable error = ( Throwable ) request.getAttribute ( "javax.servlet.error.exception" );
        if ( error == null ) {
            error = ( Throwable ) request.getAttribute ( "javax.servlet.jsp.jspException" );
            if ( error != null ) {
                request.setAttribute ( "javax.servlet.error.exception", ( Object ) error );
            }
        }
        return error;
    }
    public static boolean coerceToBoolean ( final String s ) {
        return s != null && s.length() != 0 && Boolean.parseBoolean ( s );
    }
    public static byte coerceToByte ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0;
        }
        return Byte.parseByte ( s );
    }
    public static char coerceToChar ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return '\0';
        }
        return s.charAt ( 0 );
    }
    public static double coerceToDouble ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0.0;
        }
        return Double.parseDouble ( s );
    }
    public static float coerceToFloat ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0.0f;
        }
        return Float.parseFloat ( s );
    }
    public static int coerceToInt ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0;
        }
        return Integer.parseInt ( s );
    }
    public static short coerceToShort ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0;
        }
        return Short.parseShort ( s );
    }
    public static long coerceToLong ( final String s ) {
        if ( s == null || s.length() == 0 ) {
            return 0L;
        }
        return Long.parseLong ( s );
    }
    public static Object coerce ( String s, final Class<?> target ) {
        final boolean isNullOrEmpty = s == null || s.length() == 0;
        if ( target == Boolean.class ) {
            if ( isNullOrEmpty ) {
                s = "false";
            }
            return Boolean.valueOf ( s );
        }
        if ( target == Byte.class ) {
            if ( isNullOrEmpty ) {
                return 0;
            }
            return Byte.valueOf ( s );
        } else if ( target == Character.class ) {
            if ( isNullOrEmpty ) {
                return '\0';
            }
            final Character result = s.charAt ( 0 );
            return result;
        } else if ( target == Double.class ) {
            if ( isNullOrEmpty ) {
                return 0.0;
            }
            return Double.valueOf ( s );
        } else if ( target == Float.class ) {
            if ( isNullOrEmpty ) {
                return 0.0f;
            }
            return Float.valueOf ( s );
        } else if ( target == Integer.class ) {
            if ( isNullOrEmpty ) {
                return 0;
            }
            return Integer.valueOf ( s );
        } else if ( target == Short.class ) {
            if ( isNullOrEmpty ) {
                return 0;
            }
            return Short.valueOf ( s );
        } else {
            if ( target != Long.class ) {
                return null;
            }
            if ( isNullOrEmpty ) {
                return 0L;
            }
            return Long.valueOf ( s );
        }
    }
    public static Object convert ( final String propertyName, String s, final Class<?> t, final Class<?> propertyEditorClass ) throws JasperException {
        try {
            if ( s == null ) {
                if ( !t.equals ( Boolean.class ) && !t.equals ( Boolean.TYPE ) ) {
                    return null;
                }
                s = "false";
            }
            if ( propertyEditorClass != null ) {
                return getValueFromBeanInfoPropertyEditor ( t, propertyName, s, propertyEditorClass );
            }
            if ( t.equals ( Boolean.class ) || t.equals ( Boolean.TYPE ) ) {
                if ( s.equalsIgnoreCase ( "on" ) || s.equalsIgnoreCase ( "true" ) ) {
                    s = "true";
                } else {
                    s = "false";
                }
                return Boolean.valueOf ( s );
            }
            if ( t.equals ( Byte.class ) || t.equals ( Byte.TYPE ) ) {
                return Byte.valueOf ( s );
            }
            if ( t.equals ( Character.class ) || t.equals ( Character.TYPE ) ) {
                return ( s.length() > 0 ) ? s.charAt ( 0 ) : null;
            }
            if ( t.equals ( Short.class ) || t.equals ( Short.TYPE ) ) {
                return Short.valueOf ( s );
            }
            if ( t.equals ( Integer.class ) || t.equals ( Integer.TYPE ) ) {
                return Integer.valueOf ( s );
            }
            if ( t.equals ( Float.class ) || t.equals ( Float.TYPE ) ) {
                return Float.valueOf ( s );
            }
            if ( t.equals ( Long.class ) || t.equals ( Long.TYPE ) ) {
                return Long.valueOf ( s );
            }
            if ( t.equals ( Double.class ) || t.equals ( Double.TYPE ) ) {
                return Double.valueOf ( s );
            }
            if ( t.equals ( String.class ) ) {
                return s;
            }
            if ( t.equals ( File.class ) ) {
                return new File ( s );
            }
            if ( t.getName().equals ( "java.lang.Object" ) ) {
                return new Object[] { s };
            }
            return getValueFromPropertyEditorManager ( t, propertyName, s );
        } catch ( Exception ex ) {
            throw new JasperException ( ex );
        }
    }
    public static void introspect ( final Object bean, final ServletRequest request ) throws JasperException {
        final Enumeration<String> e = ( Enumeration<String> ) request.getParameterNames();
        while ( e.hasMoreElements() ) {
            final String name = e.nextElement();
            final String value = request.getParameter ( name );
            introspecthelper ( bean, name, value, request, name, true );
        }
    }
    public static void introspecthelper ( final Object bean, final String prop, final String value, final ServletRequest request, final String param, final boolean ignoreMethodNF ) throws JasperException {
        Method method = null;
        Class<?> type = null;
        Class<?> propertyEditorClass = null;
        try {
            final BeanInfo info = Introspector.getBeanInfo ( bean.getClass() );
            if ( info != null ) {
                final PropertyDescriptor[] pd = info.getPropertyDescriptors();
                for ( int i = 0; i < pd.length; ++i ) {
                    if ( pd[i].getName().equals ( prop ) ) {
                        method = pd[i].getWriteMethod();
                        type = pd[i].getPropertyType();
                        propertyEditorClass = pd[i].getPropertyEditorClass();
                        break;
                    }
                }
            }
            if ( method != null && type != null ) {
                if ( type.isArray() ) {
                    if ( request == null ) {
                        throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.setproperty.noindexset" ) );
                    }
                    final Class<?> t = type.getComponentType();
                    final String[] values = request.getParameterValues ( param );
                    if ( values == null ) {
                        return;
                    }
                    if ( t.equals ( String.class ) ) {
                        method.invoke ( bean, values );
                    } else {
                        createTypedArray ( prop, bean, method, values, t, propertyEditorClass );
                    }
                } else {
                    if ( value == null || ( param != null && value.equals ( "" ) ) ) {
                        return;
                    }
                    final Object oval = convert ( prop, value, type, propertyEditorClass );
                    if ( oval != null ) {
                        method.invoke ( bean, oval );
                    }
                }
            }
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
        if ( ignoreMethodNF || method != null ) {
            return;
        }
        if ( type == null ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.noproperty", prop, bean.getClass().getName() ) );
        }
        throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nomethod.setproperty", prop, type.getName(), bean.getClass().getName() ) );
    }
    public static String toString ( final Object o ) {
        return String.valueOf ( o );
    }
    public static String toString ( final byte b ) {
        return Byte.toString ( b );
    }
    public static String toString ( final boolean b ) {
        return Boolean.toString ( b );
    }
    public static String toString ( final short s ) {
        return Short.toString ( s );
    }
    public static String toString ( final int i ) {
        return Integer.toString ( i );
    }
    public static String toString ( final float f ) {
        return Float.toString ( f );
    }
    public static String toString ( final long l ) {
        return Long.toString ( l );
    }
    public static String toString ( final double d ) {
        return Double.toString ( d );
    }
    public static String toString ( final char c ) {
        return Character.toString ( c );
    }
    public static void createTypedArray ( final String propertyName, final Object bean, final Method method, final String[] values, final Class<?> t, final Class<?> propertyEditorClass ) throws JasperException {
        try {
            if ( propertyEditorClass != null ) {
                final Object[] tmpval = new Integer[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval[i] = getValueFromBeanInfoPropertyEditor ( t, propertyName, values[i], propertyEditorClass );
                }
                method.invoke ( bean, tmpval );
            } else if ( t.equals ( Integer.class ) ) {
                final Integer[] tmpval2 = new Integer[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval2[i] = Integer.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval2 );
            } else if ( t.equals ( Byte.class ) ) {
                final Byte[] tmpval3 = new Byte[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval3[i] = Byte.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval3 );
            } else if ( t.equals ( Boolean.class ) ) {
                final Boolean[] tmpval4 = new Boolean[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval4[i] = Boolean.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval4 );
            } else if ( t.equals ( Short.class ) ) {
                final Short[] tmpval5 = new Short[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval5[i] = Short.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval5 );
            } else if ( t.equals ( Long.class ) ) {
                final Long[] tmpval6 = new Long[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval6[i] = Long.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval6 );
            } else if ( t.equals ( Double.class ) ) {
                final Double[] tmpval7 = new Double[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval7[i] = Double.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval7 );
            } else if ( t.equals ( Float.class ) ) {
                final Float[] tmpval8 = new Float[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval8[i] = Float.valueOf ( values[i] );
                }
                method.invoke ( bean, tmpval8 );
            } else if ( t.equals ( Character.class ) ) {
                final Character[] tmpval9 = new Character[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval9[i] = values[i].charAt ( 0 );
                }
                method.invoke ( bean, tmpval9 );
            } else if ( t.equals ( Integer.TYPE ) ) {
                final int[] tmpval10 = new int[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval10[i] = Integer.parseInt ( values[i] );
                }
                method.invoke ( bean, tmpval10 );
            } else if ( t.equals ( Byte.TYPE ) ) {
                final byte[] tmpval11 = new byte[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval11[i] = Byte.parseByte ( values[i] );
                }
                method.invoke ( bean, tmpval11 );
            } else if ( t.equals ( Boolean.TYPE ) ) {
                final boolean[] tmpval12 = new boolean[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval12[i] = Boolean.parseBoolean ( values[i] );
                }
                method.invoke ( bean, tmpval12 );
            } else if ( t.equals ( Short.TYPE ) ) {
                final short[] tmpval13 = new short[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval13[i] = Short.parseShort ( values[i] );
                }
                method.invoke ( bean, tmpval13 );
            } else if ( t.equals ( Long.TYPE ) ) {
                final long[] tmpval14 = new long[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval14[i] = Long.parseLong ( values[i] );
                }
                method.invoke ( bean, tmpval14 );
            } else if ( t.equals ( Double.TYPE ) ) {
                final double[] tmpval15 = new double[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval15[i] = Double.parseDouble ( values[i] );
                }
                method.invoke ( bean, tmpval15 );
            } else if ( t.equals ( Float.TYPE ) ) {
                final float[] tmpval16 = new float[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval16[i] = Float.parseFloat ( values[i] );
                }
                method.invoke ( bean, tmpval16 );
            } else if ( t.equals ( Character.TYPE ) ) {
                final char[] tmpval17 = new char[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval17[i] = values[i].charAt ( 0 );
                }
                method.invoke ( bean, tmpval17 );
            } else {
                final Object[] tmpval = new Integer[values.length];
                for ( int i = 0; i < values.length; ++i ) {
                    tmpval[i] = getValueFromPropertyEditorManager ( t, propertyName, values[i] );
                }
                method.invoke ( bean, tmpval );
            }
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( "error in invoking method", ex );
        }
    }
    public static String escapeQueryString ( final String unescString ) {
        if ( unescString == null ) {
            return null;
        }
        final StringBuilder escStringBuilder = new StringBuilder();
        final String shellSpChars = "&;`'\"|*?~<>^()[]{}$\\\n";
        for ( int index = 0; index < unescString.length(); ++index ) {
            final char nextChar = unescString.charAt ( index );
            if ( shellSpChars.indexOf ( nextChar ) != -1 ) {
                escStringBuilder.append ( '\\' );
            }
            escStringBuilder.append ( nextChar );
        }
        return escStringBuilder.toString();
    }
    public static Object handleGetProperty ( final Object o, final String prop ) throws JasperException {
        if ( o == null ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nullbean" ) );
        }
        Object value = null;
        try {
            final Method method = getReadMethod ( o.getClass(), prop );
            value = method.invoke ( o, ( Object[] ) null );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
        return value;
    }
    public static void handleSetPropertyExpression ( final Object bean, final String prop, final String expression, final PageContext pageContext, final ProtectedFunctionMapper functionMapper ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, PageContextImpl.proprietaryEvaluate ( expression, method.getParameterTypes() [0], pageContext, functionMapper ) );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final Object value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final int value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final short value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final long value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final double value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final float value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final char value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final byte value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static void handleSetProperty ( final Object bean, final String prop, final boolean value ) throws JasperException {
        try {
            final Method method = getWriteMethod ( bean.getClass(), prop );
            method.invoke ( bean, value );
        } catch ( Exception ex ) {
            final Throwable thr = ExceptionUtils.unwrapInvocationTargetException ( ex );
            ExceptionUtils.handleThrowable ( thr );
            throw new JasperException ( ex );
        }
    }
    public static Method getWriteMethod ( final Class<?> beanClass, final String prop ) throws JasperException {
        Method method = null;
        Class<?> type = null;
        try {
            final BeanInfo info = Introspector.getBeanInfo ( beanClass );
            if ( info == null ) {
                throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nobeaninfo", beanClass.getName() ) );
            }
            final PropertyDescriptor[] pd = info.getPropertyDescriptors();
            for ( int i = 0; i < pd.length; ++i ) {
                if ( pd[i].getName().equals ( prop ) ) {
                    method = pd[i].getWriteMethod();
                    type = pd[i].getPropertyType();
                    break;
                }
            }
        } catch ( Exception ex ) {
            throw new JasperException ( ex );
        }
        if ( method != null ) {
            return method;
        }
        if ( type == null ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.noproperty", prop, beanClass.getName() ) );
        }
        throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nomethod.setproperty", prop, type.getName(), beanClass.getName() ) );
    }
    public static Method getReadMethod ( final Class<?> beanClass, final String prop ) throws JasperException {
        Method method = null;
        Class<?> type = null;
        try {
            final BeanInfo info = Introspector.getBeanInfo ( beanClass );
            if ( info == null ) {
                throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nobeaninfo", beanClass.getName() ) );
            }
            final PropertyDescriptor[] pd = info.getPropertyDescriptors();
            for ( int i = 0; i < pd.length; ++i ) {
                if ( pd[i].getName().equals ( prop ) ) {
                    method = pd[i].getReadMethod();
                    type = pd[i].getPropertyType();
                    break;
                }
            }
        } catch ( Exception ex ) {
            throw new JasperException ( ex );
        }
        if ( method != null ) {
            return method;
        }
        if ( type == null ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.noproperty", prop, beanClass.getName() ) );
        }
        throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.nomethod", prop, beanClass.getName() ) );
    }
    public static Object getValueFromBeanInfoPropertyEditor ( final Class<?> attrClass, final String attrName, final String attrValue, final Class<?> propertyEditorClass ) throws JasperException {
        try {
            final PropertyEditor pe = ( PropertyEditor ) propertyEditorClass.newInstance();
            pe.setAsText ( attrValue );
            return pe.getValue();
        } catch ( Exception ex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.property.conversion", attrValue, attrClass.getName(), attrName, ex.getMessage() ) );
        }
    }
    public static Object getValueFromPropertyEditorManager ( final Class<?> attrClass, final String attrName, final String attrValue ) throws JasperException {
        try {
            final PropertyEditor propEditor = PropertyEditorManager.findEditor ( attrClass );
            if ( propEditor != null ) {
                propEditor.setAsText ( attrValue );
                return propEditor.getValue();
            }
            throw new IllegalArgumentException ( Localizer.getMessage ( "jsp.error.beans.propertyeditor.notregistered" ) );
        } catch ( IllegalArgumentException ex ) {
            throw new JasperException ( Localizer.getMessage ( "jsp.error.beans.property.conversion", attrValue, attrClass.getName(), attrName, ex.getMessage() ) );
        }
    }
    public static String getContextRelativePath ( final ServletRequest request, final String relativePath ) {
        if ( relativePath.startsWith ( "/" ) ) {
            return relativePath;
        }
        if ( ! ( request instanceof HttpServletRequest ) ) {
            return relativePath;
        }
        final HttpServletRequest hrequest = ( HttpServletRequest ) request;
        String uri = ( String ) request.getAttribute ( "javax.servlet.include.servlet_path" );
        if ( uri != null ) {
            final String pathInfo = ( String ) request.getAttribute ( "javax.servlet.include.path_info" );
            if ( pathInfo == null && uri.lastIndexOf ( 47 ) >= 0 ) {
                uri = uri.substring ( 0, uri.lastIndexOf ( 47 ) );
            }
        } else {
            uri = hrequest.getServletPath();
            if ( uri.lastIndexOf ( 47 ) >= 0 ) {
                uri = uri.substring ( 0, uri.lastIndexOf ( 47 ) );
            }
        }
        return uri + '/' + relativePath;
    }
    public static void include ( final ServletRequest request, final ServletResponse response, final String relativePath, final JspWriter out, final boolean flush ) throws IOException, ServletException {
        if ( flush && ! ( out instanceof BodyContent ) ) {
            out.flush();
        }
        final String resourcePath = getContextRelativePath ( request, relativePath );
        final RequestDispatcher rd = request.getRequestDispatcher ( resourcePath );
        rd.include ( request, ( ServletResponse ) new ServletResponseWrapperInclude ( response, out ) );
    }
    public static String URLEncode ( final String s, String enc ) {
        if ( s == null ) {
            return "null";
        }
        if ( enc == null ) {
            enc = "ISO-8859-1";
        }
        final StringBuilder out = new StringBuilder ( s.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter ( buf, enc );
        } catch ( UnsupportedEncodingException ex ) {
            writer = new OutputStreamWriter ( buf );
        }
        for ( int i = 0; i < s.length(); ++i ) {
            final int c = s.charAt ( i );
            if ( c == 32 ) {
                out.append ( '+' );
            } else if ( isSafeChar ( c ) ) {
                out.append ( ( char ) c );
            } else {
                try {
                    writer.write ( c );
                    writer.flush();
                } catch ( IOException e ) {
                    buf.reset();
                    continue;
                }
                final byte[] ba = buf.toByteArray();
                for ( int j = 0; j < ba.length; ++j ) {
                    out.append ( '%' );
                    out.append ( Character.forDigit ( ba[j] >> 4 & 0xF, 16 ) );
                    out.append ( Character.forDigit ( ba[j] & 0xF, 16 ) );
                }
                buf.reset();
            }
        }
        return out.toString();
    }
    private static boolean isSafeChar ( final int c ) {
        return ( c >= 97 && c <= 122 ) || ( c >= 65 && c <= 90 ) || ( c >= 48 && c <= 57 ) || ( c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40 || c == 41 );
    }
}
