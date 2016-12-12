package org.apache.jasper.compiler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.Jar;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
public class JspUtil {
    private static final String WEB_INF_TAGS = "/WEB-INF/tags/";
    private static final String META_INF_TAGS = "/META-INF/tags/";
    private static final String OPEN_EXPR = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String javaKeywords[] = { "abstract", "assert",
                                                   "boolean", "break", "byte", "case", "catch", "char", "class",
                                                   "const", "continue", "default", "do", "double", "else", "enum",
                                                   "extends", "final", "finally", "float", "for", "goto", "if",
                                                   "implements", "import", "instanceof", "int", "interface", "long",
                                                   "native", "new", "package", "private", "protected", "public",
                                                   "return", "short", "static", "strictfp", "super", "switch",
                                                   "synchronized", "this", "throw", "throws", "transient", "try",
                                                   "void", "volatile", "while"
                                                 };
    public static final int CHUNKSIZE = 1024;
    public static String getExprInXml ( String expression ) {
        String returnString;
        int length = expression.length();
        if ( expression.startsWith ( OPEN_EXPR ) &&
                expression.endsWith ( CLOSE_EXPR ) ) {
            returnString = expression.substring ( 1, length - 1 );
        } else {
            returnString = expression;
        }
        return escapeXml ( returnString );
    }
    public static void checkScope ( String scope, Node n, ErrorDispatcher err )
    throws JasperException {
        if ( scope != null && !scope.equals ( "page" ) && !scope.equals ( "request" )
                && !scope.equals ( "session" ) && !scope.equals ( "application" ) ) {
            err.jspError ( n, "jsp.error.invalid.scope", scope );
        }
    }
    public static void checkAttributes ( String typeOfTag, Node n,
                                         ValidAttribute[] validAttributes, ErrorDispatcher err )
    throws JasperException {
        Attributes attrs = n.getAttributes();
        Mark start = n.getStart();
        boolean valid = true;
        int tempLength = ( attrs == null ) ? 0 : attrs.getLength();
        Vector<String> temp = new Vector<> ( tempLength, 1 );
        for ( int i = 0; i < tempLength; i++ ) {
            @SuppressWarnings ( "null" )
            String qName = attrs.getQName ( i );
            if ( ( !qName.equals ( "xmlns" ) ) && ( !qName.startsWith ( "xmlns:" ) ) ) {
                temp.addElement ( qName );
            }
        }
        Node.Nodes tagBody = n.getBody();
        if ( tagBody != null ) {
            int numSubElements = tagBody.size();
            for ( int i = 0; i < numSubElements; i++ ) {
                Node node = tagBody.getNode ( i );
                if ( node instanceof Node.NamedAttribute ) {
                    String attrName = node.getAttributeValue ( "name" );
                    temp.addElement ( attrName );
                    if ( n.getAttributeValue ( attrName ) != null ) {
                        err.jspError ( n,
                                       "jsp.error.duplicate.name.jspattribute",
                                       attrName );
                    }
                } else {
                    break;
                }
            }
        }
        String missingAttribute = null;
        for ( int i = 0; i < validAttributes.length; i++ ) {
            int attrPos;
            if ( validAttributes[i].mandatory ) {
                attrPos = temp.indexOf ( validAttributes[i].name );
                if ( attrPos != -1 ) {
                    temp.remove ( attrPos );
                    valid = true;
                } else {
                    valid = false;
                    missingAttribute = validAttributes[i].name;
                    break;
                }
            }
        }
        if ( !valid ) {
            err.jspError ( start, "jsp.error.mandatory.attribute", typeOfTag,
                           missingAttribute );
        }
        int attrLeftLength = temp.size();
        if ( attrLeftLength == 0 ) {
            return;
        }
        String attribute = null;
        for ( int j = 0; j < attrLeftLength; j++ ) {
            valid = false;
            attribute = temp.elementAt ( j );
            for ( int i = 0; i < validAttributes.length; i++ ) {
                if ( attribute.equals ( validAttributes[i].name ) ) {
                    valid = true;
                    break;
                }
            }
            if ( !valid ) {
                err.jspError ( start, "jsp.error.invalid.attribute", typeOfTag,
                               attribute );
            }
        }
    }
    public static String escapeXml ( String s ) {
        if ( s == null ) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt ( i );
            if ( c == '<' ) {
                sb.append ( "&lt;" );
            } else if ( c == '>' ) {
                sb.append ( "&gt;" );
            } else if ( c == '\'' ) {
                sb.append ( "&apos;" );
            } else if ( c == '&' ) {
                sb.append ( "&amp;" );
            } else if ( c == '"' ) {
                sb.append ( "&quot;" );
            } else {
                sb.append ( c );
            }
        }
        return sb.toString();
    }
    public static class ValidAttribute {
        private final String name;
        private final boolean mandatory;
        public ValidAttribute ( String name, boolean mandatory ) {
            this.name = name;
            this.mandatory = mandatory;
        }
        public ValidAttribute ( String name ) {
            this ( name, false );
        }
    }
    public static boolean booleanValue ( String s ) {
        boolean b = false;
        if ( s != null ) {
            if ( s.equalsIgnoreCase ( "yes" ) ) {
                b = true;
            } else {
                b = Boolean.parseBoolean ( s );
            }
        }
        return b;
    }
    public static Class<?> toClass ( String type, ClassLoader loader )
    throws ClassNotFoundException {
        Class<?> c = null;
        int i0 = type.indexOf ( '[' );
        int dims = 0;
        if ( i0 > 0 ) {
            for ( int i = 0; i < type.length(); i++ ) {
                if ( type.charAt ( i ) == '[' ) {
                    dims++;
                }
            }
            type = type.substring ( 0, i0 );
        }
        if ( "boolean".equals ( type ) ) {
            c = boolean.class;
        } else if ( "char".equals ( type ) ) {
            c = char.class;
        } else if ( "byte".equals ( type ) ) {
            c = byte.class;
        } else if ( "short".equals ( type ) ) {
            c = short.class;
        } else if ( "int".equals ( type ) ) {
            c = int.class;
        } else if ( "long".equals ( type ) ) {
            c = long.class;
        } else if ( "float".equals ( type ) ) {
            c = float.class;
        } else if ( "double".equals ( type ) ) {
            c = double.class;
        } else if ( "void".equals ( type ) ) {
            c = void.class;
        } else if ( type.indexOf ( '[' ) < 0 ) {
            c = loader.loadClass ( type );
        }
        if ( dims == 0 ) {
            return c;
        }
        if ( dims == 1 ) {
            return java.lang.reflect.Array.newInstance ( c, 1 ).getClass();
        }
        return java.lang.reflect.Array.newInstance ( c, new int[dims] ).getClass();
    }
    public static String interpreterCall ( boolean isTagFile, String expression,
                                           Class<?> expectedType, String fnmapvar ) {
        String jspCtxt = null;
        if ( isTagFile ) {
            jspCtxt = "this.getJspContext()";
        } else {
            jspCtxt = "_jspx_page_context";
        }
        String returnType = expectedType.getCanonicalName();
        String targetType = returnType;
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
        StringBuilder call = new StringBuilder (
            "("
            + returnType
            + ") "
            + "org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate"
            + "(" + Generator.quote ( expression ) + ", " + targetType
            + ".class, " + "(javax.servlet.jsp.PageContext)" + jspCtxt + ", "
            + fnmapvar + ")" );
        if ( primitiveConverterMethod != null ) {
            call.insert ( 0, "(" );
            call.append ( ")." + primitiveConverterMethod + "()" );
        }
        return call.toString();
    }
    public static String coerceToPrimitiveBoolean ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToBoolean("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "false";
            } else {
                return Boolean.valueOf ( s ).toString();
            }
        }
    }
    public static String coerceToBoolean ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Boolean) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Boolean.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Boolean(false)";
            } else {
                return "new java.lang.Boolean(" + Boolean.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToPrimitiveByte ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToByte("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(byte) 0";
            } else {
                return "((byte)" + Byte.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToByte ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Byte) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Byte.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Byte((byte) 0)";
            } else {
                return "new java.lang.Byte((byte)" + Byte.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToChar ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToChar("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(char) 0";
            } else {
                char ch = s.charAt ( 0 );
                return "((char) " + ( int ) ch + ")";
            }
        }
    }
    public static String coerceToCharacter ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Character) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Character.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Character((char) 0)";
            } else {
                char ch = s.charAt ( 0 );
                return "new java.lang.Character((char) " + ( int ) ch + ")";
            }
        }
    }
    public static String coerceToPrimitiveDouble ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToDouble("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(double) 0";
            } else {
                return Double.valueOf ( s ).toString();
            }
        }
    }
    public static String coerceToDouble ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Double) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", Double.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Double(0)";
            } else {
                return "new java.lang.Double(" + Double.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToPrimitiveFloat ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToFloat("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(float) 0";
            } else {
                return Float.valueOf ( s ).toString() + "f";
            }
        }
    }
    public static String coerceToFloat ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Float) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Float.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Float(0)";
            } else {
                return "new java.lang.Float(" + Float.valueOf ( s ).toString() + "f)";
            }
        }
    }
    public static String coerceToInt ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToInt("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "0";
            } else {
                return Integer.valueOf ( s ).toString();
            }
        }
    }
    public static String coerceToInteger ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Integer) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Integer.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Integer(0)";
            } else {
                return "new java.lang.Integer(" + Integer.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToPrimitiveShort ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToShort("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(short) 0";
            } else {
                return "((short) " + Short.valueOf ( s ).toString() + ")";
            }
        }
    }
    public static String coerceToShort ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Short) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Short.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Short((short) 0)";
            } else {
                return "new java.lang.Short(\"" + Short.valueOf ( s ).toString() + "\")";
            }
        }
    }
    public static String coerceToPrimitiveLong ( String s,
            boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "org.apache.jasper.runtime.JspRuntimeLibrary.coerceToLong("
                   + s + ")";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "(long) 0";
            } else {
                return Long.valueOf ( s ).toString() + "l";
            }
        }
    }
    public static String coerceToLong ( String s, boolean isNamedAttribute ) {
        if ( isNamedAttribute ) {
            return "(java.lang.Long) org.apache.jasper.runtime.JspRuntimeLibrary.coerce("
                   + s + ", java.lang.Long.class)";
        } else {
            if ( s == null || s.length() == 0 ) {
                return "new java.lang.Long(0)";
            } else {
                return "new java.lang.Long(" + Long.valueOf ( s ).toString() + "l)";
            }
        }
    }
    public static InputStream getInputStream ( String fname, Jar jar,
            JspCompilationContext ctxt ) throws IOException {
        InputStream in = null;
        if ( jar != null ) {
            String jarEntryName = fname.substring ( 1, fname.length() );
            in = jar.getInputStream ( jarEntryName );
        } else {
            in = ctxt.getResourceAsStream ( fname );
        }
        if ( in == null ) {
            throw new FileNotFoundException ( Localizer.getMessage (
                                                  "jsp.error.file.not.found", fname ) );
        }
        return in;
    }
    public static InputSource getInputSource ( String fname, Jar jar, JspCompilationContext ctxt )
    throws IOException {
        InputSource source;
        if ( jar != null ) {
            String jarEntryName = fname.substring ( 1, fname.length() );
            source = new InputSource ( jar.getInputStream ( jarEntryName ) );
            source.setSystemId ( jar.getURL ( jarEntryName ) );
        } else {
            source = new InputSource ( ctxt.getResourceAsStream ( fname ) );
            source.setSystemId ( ctxt.getResource ( fname ).toExternalForm() );
        }
        return source;
    }
    public static String getTagHandlerClassName ( String path, String urn,
            ErrorDispatcher err ) throws JasperException {
        String className = null;
        int begin = 0;
        int index;
        index = path.lastIndexOf ( ".tag" );
        if ( index == -1 ) {
            err.jspError ( "jsp.error.tagfile.badSuffix", path );
        }
        index = path.indexOf ( WEB_INF_TAGS );
        if ( index != -1 ) {
            className = Constants.TAG_FILE_PACKAGE_NAME + ".web.";
            begin = index + WEB_INF_TAGS.length();
        } else {
            index = path.indexOf ( META_INF_TAGS );
            if ( index != -1 ) {
                className = getClassNameBase ( urn );
                begin = index + META_INF_TAGS.length();
            } else {
                err.jspError ( "jsp.error.tagfile.illegalPath", path );
            }
        }
        className += makeJavaPackage ( path.substring ( begin ) );
        return className;
    }
    private static String getClassNameBase ( String urn ) {
        StringBuilder base =
            new StringBuilder ( Constants.TAG_FILE_PACKAGE_NAME + ".meta." );
        if ( urn != null ) {
            base.append ( makeJavaPackage ( urn ) );
            base.append ( '.' );
        }
        return base.toString();
    }
    public static final String makeJavaPackage ( String path ) {
        String classNameComponents[] = split ( path, "/" );
        StringBuilder legalClassNames = new StringBuilder();
        for ( int i = 0; i < classNameComponents.length; i++ ) {
            legalClassNames.append ( makeJavaIdentifier ( classNameComponents[i] ) );
            if ( i < classNameComponents.length - 1 ) {
                legalClassNames.append ( '.' );
            }
        }
        return legalClassNames.toString();
    }
    private static final String[] split ( String path, String pat ) {
        Vector<String> comps = new Vector<>();
        int pos = path.indexOf ( pat );
        int start = 0;
        while ( pos >= 0 ) {
            if ( pos > start ) {
                String comp = path.substring ( start, pos );
                comps.add ( comp );
            }
            start = pos + pat.length();
            pos = path.indexOf ( pat, start );
        }
        if ( start < path.length() ) {
            comps.add ( path.substring ( start ) );
        }
        String[] result = new String[comps.size()];
        for ( int i = 0; i < comps.size(); i++ ) {
            result[i] = comps.elementAt ( i );
        }
        return result;
    }
    public static final String makeJavaIdentifier ( String identifier ) {
        return makeJavaIdentifier ( identifier, true );
    }
    public static final String makeJavaIdentifierForAttribute ( String identifier ) {
        return makeJavaIdentifier ( identifier, false );
    }
    private static final String makeJavaIdentifier ( String identifier,
            boolean periodToUnderscore ) {
        StringBuilder modifiedIdentifier = new StringBuilder ( identifier.length() );
        if ( !Character.isJavaIdentifierStart ( identifier.charAt ( 0 ) ) ) {
            modifiedIdentifier.append ( '_' );
        }
        for ( int i = 0; i < identifier.length(); i++ ) {
            char ch = identifier.charAt ( i );
            if ( Character.isJavaIdentifierPart ( ch ) &&
                    ( ch != '_' || !periodToUnderscore ) ) {
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
    public static final String mangleChar ( char ch ) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit ( ( ch >> 12 ) & 0xf, 16 );
        result[2] = Character.forDigit ( ( ch >> 8 ) & 0xf, 16 );
        result[3] = Character.forDigit ( ( ch >> 4 ) & 0xf, 16 );
        result[4] = Character.forDigit ( ch & 0xf, 16 );
        return new String ( result );
    }
    public static boolean isJavaKeyword ( String key ) {
        int i = 0;
        int j = javaKeywords.length;
        while ( i < j ) {
            int k = ( i + j ) / 2;
            int result = javaKeywords[k].compareTo ( key );
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
    static InputStreamReader getReader ( String fname, String encoding,
                                         Jar jar, JspCompilationContext ctxt, ErrorDispatcher err )
    throws JasperException, IOException {
        return getReader ( fname, encoding, jar, ctxt, err, 0 );
    }
    static InputStreamReader getReader ( String fname, String encoding,
                                         Jar jar, JspCompilationContext ctxt, ErrorDispatcher err, int skip )
    throws JasperException, IOException {
        InputStreamReader reader = null;
        InputStream in = getInputStream ( fname, jar, ctxt );
        for ( int i = 0; i < skip; i++ ) {
            in.read();
        }
        try {
            reader = new InputStreamReader ( in, encoding );
        } catch ( UnsupportedEncodingException ex ) {
            err.jspError ( "jsp.error.unsupported.encoding", encoding );
        }
        return reader;
    }
    public static String toJavaSourceTypeFromTld ( String type ) {
        if ( type == null || "void".equals ( type ) ) {
            return "java.lang.Void.TYPE";
        }
        return type + ".class";
    }
    public static String toJavaSourceType ( String type ) {
        if ( type.charAt ( 0 ) != '[' ) {
            return type;
        }
        int dims = 1;
        String t = null;
        for ( int i = 1; i < type.length(); i++ ) {
            if ( type.charAt ( i ) == '[' ) {
                dims++;
            } else {
                switch ( type.charAt ( i ) ) {
                case 'Z':
                    t = "boolean";
                    break;
                case 'B':
                    t = "byte";
                    break;
                case 'C':
                    t = "char";
                    break;
                case 'D':
                    t = "double";
                    break;
                case 'F':
                    t = "float";
                    break;
                case 'I':
                    t = "int";
                    break;
                case 'J':
                    t = "long";
                    break;
                case 'S':
                    t = "short";
                    break;
                case 'L':
                    t = type.substring ( i + 1, type.indexOf ( ';' ) );
                    break;
                }
                break;
            }
        }
        if ( t == null ) {
            throw new IllegalArgumentException ( "Unable to extract type from [" +
                                                 type + "]" );
        }
        StringBuilder resultType = new StringBuilder ( t );
        for ( ; dims > 0; dims-- ) {
            resultType.append ( "[]" );
        }
        return resultType.toString();
    }
}
