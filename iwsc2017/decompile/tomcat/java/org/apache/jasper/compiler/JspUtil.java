package org.apache.jasper.compiler;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import org.apache.jasper.Constants;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.Jar;
import java.lang.reflect.Array;
import org.xml.sax.Attributes;
import java.util.Vector;
import org.apache.jasper.JasperException;
public class JspUtil {
    private static final String WEB_INF_TAGS = "/WEB-INF/tags/";
    private static final String META_INF_TAGS = "/META-INF/tags/";
    private static final String OPEN_EXPR = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String[] javaKeywords;
    public static final int CHUNKSIZE = 1024;
    public static String getExprInXml ( final String expression ) {
        final int length = expression.length();
        String returnString;
        if ( expression.startsWith ( "<%=" ) && expression.endsWith ( "%>" ) ) {
            returnString = expression.substring ( 1, length - 1 );
        } else {
            returnString = expression;
        }
        return escapeXml ( returnString );
    }
    public static void checkScope ( final String scope, final Node n, final ErrorDispatcher err ) throws JasperException {
        if ( scope != null && !scope.equals ( "page" ) && !scope.equals ( "request" ) && !scope.equals ( "session" ) && !scope.equals ( "application" ) ) {
            err.jspError ( n, "jsp.error.invalid.scope", scope );
        }
    }
    public static void checkAttributes ( final String typeOfTag, final Node n, final ValidAttribute[] validAttributes, final ErrorDispatcher err ) throws JasperException {
        final Attributes attrs = n.getAttributes();
        final Mark start = n.getStart();
        boolean valid = true;
        final int tempLength = ( attrs == null ) ? 0 : attrs.getLength();
        final Vector<String> temp = new Vector<String> ( tempLength, 1 );
        for ( int i = 0; i < tempLength; ++i ) {
            final String qName = attrs.getQName ( i );
            if ( !qName.equals ( "xmlns" ) && !qName.startsWith ( "xmlns:" ) ) {
                temp.addElement ( qName );
            }
        }
        final Node.Nodes tagBody = n.getBody();
        if ( tagBody != null ) {
            for ( int numSubElements = tagBody.size(), j = 0; j < numSubElements; ++j ) {
                final Node node = tagBody.getNode ( j );
                if ( ! ( node instanceof Node.NamedAttribute ) ) {
                    break;
                }
                final String attrName = node.getAttributeValue ( "name" );
                temp.addElement ( attrName );
                if ( n.getAttributeValue ( attrName ) != null ) {
                    err.jspError ( n, "jsp.error.duplicate.name.jspattribute", attrName );
                }
            }
        }
        String missingAttribute = null;
        for ( int j = 0; j < validAttributes.length; ++j ) {
            if ( validAttributes[j].mandatory ) {
                final int attrPos = temp.indexOf ( validAttributes[j].name );
                if ( attrPos == -1 ) {
                    valid = false;
                    missingAttribute = validAttributes[j].name;
                    break;
                }
                temp.remove ( attrPos );
                valid = true;
            }
        }
        if ( !valid ) {
            err.jspError ( start, "jsp.error.mandatory.attribute", typeOfTag, missingAttribute );
        }
        final int attrLeftLength = temp.size();
        if ( attrLeftLength == 0 ) {
            return;
        }
        String attribute = null;
        for ( int k = 0; k < attrLeftLength; ++k ) {
            valid = false;
            attribute = temp.elementAt ( k );
            for ( int l = 0; l < validAttributes.length; ++l ) {
                if ( attribute.equals ( validAttributes[l].name ) ) {
                    valid = true;
                    break;
                }
            }
            if ( !valid ) {
                err.jspError ( start, "jsp.error.invalid.attribute", typeOfTag, attribute );
            }
        }
    }
    public static String escapeXml ( final String s ) {
        if ( s == null ) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < s.length(); ++i ) {
            final char c = s.charAt ( i );
            if ( c == '<' ) {
                sb.append ( "&lt;" );
            } else if ( c == '>' ) {
                sb.append ( "&gt;" );
            } else if ( c == '\'' ) {
                sb.append ( "&apos;" );
            } else if ( c == '&' ) {
                sb.append ( "&amp;" );
            } else if ( c == '\"' ) {
                sb.append ( "&quot;" );
            } else {
                sb.append ( c );
            }
        }
        return sb.toString();
    }
    public static boolean booleanValue ( final String s ) {
        boolean b = false;
        if ( s != null ) {
            b = ( s.equalsIgnoreCase ( "yes" ) || Boolean.parseBoolean ( s ) );
        }
        return b;
    }
    public static Class<?> toClass ( String type, final ClassLoader loader ) throws ClassNotFoundException {
        Class<?> c = null;
        final int i0 = type.indexOf ( 91 );
        int dims = 0;
        if ( i0 > 0 ) {
            for ( int j = 0; j < type.length(); ++j ) {
                if ( type.charAt ( j ) == '[' ) {
                    ++dims;
                }
            }
            type = type.substring ( 0, i0 );
        }
        if ( "boolean".equals ( type ) ) {
            c = Boolean.TYPE;
        } else if ( "char".equals ( type ) ) {
            c = Character.TYPE;
        } else if ( "byte".equals ( type ) ) {
            c = Byte.TYPE;
        } else if ( "short".equals ( type ) ) {
            c = Short.TYPE;
        } else if ( "int".equals ( type ) ) {
            c = Integer.TYPE;
        } else if ( "long".equals ( type ) ) {
            c = Long.TYPE;
        } else if ( "float".equals ( type ) ) {
            c = Float.TYPE;
        } else if ( "double".equals ( type ) ) {
            c = Double.TYPE;
        } else if ( "void".equals ( type ) ) {
            c = Void.TYPE;
        } else if ( type.indexOf ( 91 ) < 0 ) {
            c = loader.loadClass ( type );
        }
        if ( dims == 0 ) {
            return c;
        }
        if ( dims == 1 ) {
            return Array.newInstance ( c, 1 ).getClass();
        }
        return Array.newInstance ( c, new int[dims] ).getClass();
    }
    public static String interpreterCall ( final boolean isTagFile, final String expression, final Class<?> expectedType, final String fnmapvar ) {
        String jspCtxt = null;
        if ( isTagFile ) {
            jspCtxt = "this.getJspContext()";
        } else {
            jspCtxt = "_jspx_page_context";
        }
        String targetType;
        String returnType = targetType = expectedType.getCanonicalName();
        String primitiveConverterMethod = null;
        if ( expectedType.isPrimitive() ) {
            if ( expectedType.equals ( Boolean.TYPE ) ) {
                returnType = Boolean.class.getName();
                primitiveConverterMethod = "booleanValue";
            } else if ( expectedType.equals ( Byte.TYPE ) ) {
                returnType = Byte.class.getName();
                primitiveConverterMethod = "byteValue";
            } else if ( expectedType.equals ( Character.TYPE ) ) {
                returnType = Character.class.getName();
                primitiveConverterMethod = "charValue";
            } else if ( expectedType.equals ( Short.TYPE ) ) {
                returnType = Short.class.getName();
                primitiveConverterMethod = "shortValue";
            } else if ( expectedType.equals ( Integer.TYPE ) ) {
                returnType = Integer.class.getName();
                primitiveConverterMethod = "intValue";
            } else if ( expectedType.equals ( Long.TYPE ) ) {
                returnType = Long.class.getName();
                primitiveConverterMethod = "longValue";
            } else if ( expectedType.equals ( Float.TYPE ) ) {
                returnType = Float.class.getName();
                primitiveConverterMethod = "floatValue";
            } else if ( expectedType.equals ( Double.TYPE ) ) {
                returnType = Double.class.getName();
                primitiveConverterMethod = "doubleValue";
            }
        }
        targetType = toJavaSourceType ( targetType );
        final StringBuilder call = new StringBuilder ( "(" + returnType + ") org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate(" + Generator.quote ( expression ) + ", " + targetType + ".class, (javax.servlet.jsp.PageContext)" + jspCtxt + ", " + fnmapvar + ")" );
        if ( primitiveConverterMethod != null ) {
            call.insert ( 0, "(" );
            call.append ( ")." + primitiveConverterMethod + "()" );
        }
        return call.toString();
    }
    public static String coerceToPrimitiveBoolean ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToBoolean(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "false";
        }
        return Boolean.valueOf ( s ).toString();
    }
    public static String coerceToBoolean ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Boolean) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Boolean.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Boolean(false)";
        }
        return "new java.lang.Boolean(" + Boolean.valueOf ( s ).toString() + ")";
    }
    public static String coerceToPrimitiveByte ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToByte(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(byte) 0";
        }
        return "((byte)" + Byte.valueOf ( s ).toString() + ")";
    }
    public static String coerceToByte ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Byte) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Byte.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Byte((byte) 0)";
        }
        return "new java.lang.Byte((byte)" + Byte.valueOf ( s ).toString() + ")";
    }
    public static String coerceToChar ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToChar(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(char) 0";
        }
        final char ch = s.charAt ( 0 );
        return "((char) " + ( int ) ch + ")";
    }
    public static String coerceToCharacter ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Character) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Character.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Character((char) 0)";
        }
        final char ch = s.charAt ( 0 );
        return "new java.lang.Character((char) " + ( int ) ch + ")";
    }
    public static String coerceToPrimitiveDouble ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToDouble(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(double) 0";
        }
        return Double.valueOf ( s ).toString();
    }
    public static String coerceToDouble ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Double) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Double.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Double(0)";
        }
        return "new java.lang.Double(" + Double.valueOf ( s ).toString() + ")";
    }
    public static String coerceToPrimitiveFloat ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToFloat(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(float) 0";
        }
        return Float.valueOf ( s ).toString() + "f";
    }
    public static String coerceToFloat ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Float) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Float.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Float(0)";
        }
        return "new java.lang.Float(" + Float.valueOf ( s ).toString() + "f)";
    }
    public static String coerceToInt ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToInt(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "0";
        }
        return Integer.valueOf ( s ).toString();
    }
    public static String coerceToInteger ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Integer) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Integer.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Integer(0)";
        }
        return "new java.lang.Integer(" + Integer.valueOf ( s ).toString() + ")";
    }
    public static String coerceToPrimitiveShort ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToShort(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(short) 0";
        }
        return "((short) " + Short.valueOf ( s ).toString() + ")";
    }
    public static String coerceToShort ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Short) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Short.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Short((short) 0)";
        }
        return "new java.lang.Short(\"" + Short.valueOf ( s ).toString() + "\")";
    }
    public static String coerceToPrimitiveLong ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToLong(" + s + ")";
        }
        if ( s == null || s.length() == 0 ) {
            return "(long) 0";
        }
        return Long.valueOf ( s ).toString() + "l";
    }
    public static String coerceToLong ( final String s, final boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Long) org.apache.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", java.lang.Long.class)";
        }
        if ( s == null || s.length() == 0 ) {
            return "new java.lang.Long(0)";
        }
        return "new java.lang.Long(" + Long.valueOf ( s ).toString() + "l)";
    }
    public static InputStream getInputStream ( final String fname, final Jar jar, final JspCompilationContext ctxt ) throws IOException {
        InputStream in = null;
        if ( jar != null ) {
            final String jarEntryName = fname.substring ( 1, fname.length() );
            in = jar.getInputStream ( jarEntryName );
        } else {
            in = ctxt.getResourceAsStream ( fname );
        }
        if ( in == null ) {
            throw new FileNotFoundException ( Localizer.getMessage ( "jsp.error.file.not.found", fname ) );
        }
        return in;
    }
    public static InputSource getInputSource ( final String fname, final Jar jar, final JspCompilationContext ctxt ) throws IOException {
        InputSource source;
        if ( jar != null ) {
            final String jarEntryName = fname.substring ( 1, fname.length() );
            source = new InputSource ( jar.getInputStream ( jarEntryName ) );
            source.setSystemId ( jar.getURL ( jarEntryName ) );
        } else {
            source = new InputSource ( ctxt.getResourceAsStream ( fname ) );
            source.setSystemId ( ctxt.getResource ( fname ).toExternalForm() );
        }
        return source;
    }
    public static String getTagHandlerClassName ( final String path, final String urn, final ErrorDispatcher err ) throws JasperException {
        String className = null;
        int begin = 0;
        int index = path.lastIndexOf ( ".tag" );
        if ( index == -1 ) {
            err.jspError ( "jsp.error.tagfile.badSuffix", path );
        }
        index = path.indexOf ( "/WEB-INF/tags/" );
        if ( index != -1 ) {
            className = Constants.TAG_FILE_PACKAGE_NAME + ".web.";
            begin = index + "/WEB-INF/tags/".length();
        } else {
            index = path.indexOf ( "/META-INF/tags/" );
            if ( index != -1 ) {
                className = getClassNameBase ( urn );
                begin = index + "/META-INF/tags/".length();
            } else {
                err.jspError ( "jsp.error.tagfile.illegalPath", path );
            }
        }
        className += makeJavaPackage ( path.substring ( begin ) );
        return className;
    }
    private static String getClassNameBase ( final String urn ) {
        final StringBuilder base = new StringBuilder ( Constants.TAG_FILE_PACKAGE_NAME + ".meta." );
        if ( urn != null ) {
            base.append ( makeJavaPackage ( urn ) );
            base.append ( '.' );
        }
        return base.toString();
    }
    public static final String makeJavaPackage ( final String path ) {
        final String[] classNameComponents = split ( path, "/" );
        final StringBuilder legalClassNames = new StringBuilder();
        for ( int i = 0; i < classNameComponents.length; ++i ) {
            legalClassNames.append ( makeJavaIdentifier ( classNameComponents[i] ) );
            if ( i < classNameComponents.length - 1 ) {
                legalClassNames.append ( '.' );
            }
        }
        return legalClassNames.toString();
    }
    private static final String[] split ( final String path, final String pat ) {
        final Vector<String> comps = new Vector<String>();
        int pos;
        int start;
        for ( pos = path.indexOf ( pat ), start = 0; pos >= 0; pos = path.indexOf ( pat, start ) ) {
            if ( pos > start ) {
                final String comp = path.substring ( start, pos );
                comps.add ( comp );
            }
            start = pos + pat.length();
        }
        if ( start < path.length() ) {
            comps.add ( path.substring ( start ) );
        }
        final String[] result = new String[comps.size()];
        for ( int i = 0; i < comps.size(); ++i ) {
            result[i] = comps.elementAt ( i );
        }
        return result;
    }
    public static final String makeJavaIdentifier ( final String identifier ) {
        return makeJavaIdentifier ( identifier, true );
    }
    public static final String makeJavaIdentifierForAttribute ( final String identifier ) {
        return makeJavaIdentifier ( identifier, false );
    }
    private static final String makeJavaIdentifier ( final String identifier, final boolean periodToUnderscore ) {
        final StringBuilder modifiedIdentifier = new StringBuilder ( identifier.length() );
        if ( !Character.isJavaIdentifierStart ( identifier.charAt ( 0 ) ) ) {
            modifiedIdentifier.append ( '_' );
        }
        for ( int i = 0; i < identifier.length(); ++i ) {
            final char ch = identifier.charAt ( i );
            if ( Character.isJavaIdentifierPart ( ch ) && ( ch != '_' || !periodToUnderscore ) ) {
                modifiedIdentifier.append ( ch );
            } else if ( ch == '.' && periodToUnderscore ) {
                modifiedIdentifier.append ( '_' );
            } else {
                modifiedIdentifier.append ( mangleChar ( ch ) );
            }
        }
        if ( isJavaKeyword ( modifiedIdentifier.toString() ) ) {
            modifiedIdentifier.append ( '_' );
        }
        return modifiedIdentifier.toString();
    }
    public static final String mangleChar ( final char ch ) {
        final char[] result = { '_', Character.forDigit ( ch >> 12 & '\u000f', 16 ), Character.forDigit ( ch >> 8 & '\u000f', 16 ), Character.forDigit ( ch >> 4 & '\u000f', 16 ), Character.forDigit ( ch & '\u000f', 16 ) };
        return new String ( result );
    }
    public static boolean isJavaKeyword ( final String key ) {
        int i = 0;
        int j = JspUtil.javaKeywords.length;
        while ( i < j ) {
            final int k = ( i + j ) / 2;
            final int result = JspUtil.javaKeywords[k].compareTo ( key );
            if ( result == 0 ) {
                return true;
            }
            if ( result < 0 ) {
                i = k + 1;
            } else {
                j = k;
            }
        }
        return false;
    }
    static InputStreamReader getReader ( final String fname, final String encoding, final Jar jar, final JspCompilationContext ctxt, final ErrorDispatcher err ) throws JasperException, IOException {
        return getReader ( fname, encoding, jar, ctxt, err, 0 );
    }
    static InputStreamReader getReader ( final String fname, final String encoding, final Jar jar, final JspCompilationContext ctxt, final ErrorDispatcher err, final int skip ) throws JasperException, IOException {
        InputStreamReader reader = null;
        final InputStream in = getInputStream ( fname, jar, ctxt );
        for ( int i = 0; i < skip; ++i ) {
            in.read();
        }
        try {
            reader = new InputStreamReader ( in, encoding );
        } catch ( UnsupportedEncodingException ex ) {
            err.jspError ( "jsp.error.unsupported.encoding", encoding );
        }
        return reader;
    }
    public static String toJavaSourceTypeFromTld ( final String type ) {
        if ( type == null || "void".equals ( type ) ) {
            return "java.lang.Void.TYPE";
        }
        return type + ".class";
    }
    public static String toJavaSourceType ( final String type ) {
        if ( type.charAt ( 0 ) != '[' ) {
            return type;
        }
        int dims = 1;
        String t = null;
        for ( int i = 1; i < type.length(); ++i ) {
            if ( type.charAt ( i ) != '[' ) {
                switch ( type.charAt ( i ) ) {
                case 'Z': {
                    t = "boolean";
                    break;
                }
                case 'B': {
                    t = "byte";
                    break;
                }
                case 'C': {
                    t = "char";
                    break;
                }
                case 'D': {
                    t = "double";
                    break;
                }
                case 'F': {
                    t = "float";
                    break;
                }
                case 'I': {
                    t = "int";
                    break;
                }
                case 'J': {
                    t = "long";
                    break;
                }
                case 'S': {
                    t = "short";
                    break;
                }
                case 'L': {
                    t = type.substring ( i + 1, type.indexOf ( 59 ) );
                    break;
                }
                }
                break;
            }
            ++dims;
        }
        if ( t == null ) {
            throw new IllegalArgumentException ( "Unable to extract type from [" + type + "]" );
        }
        final StringBuilder resultType = new StringBuilder ( t );
        while ( dims > 0 ) {
            resultType.append ( "[]" );
            --dims;
        }
        return resultType.toString();
    }
    static {
        javaKeywords = new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
    }
    public static class ValidAttribute {
        private final String name;
        private final boolean mandatory;
        public ValidAttribute ( final String name, final boolean mandatory ) {
            this.name = name;
            this.mandatory = mandatory;
        }
        public ValidAttribute ( final String name ) {
            this ( name, false );
        }
    }
}
