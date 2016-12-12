package org.apache.jasper.compiler;
import java.io.InputStream;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import java.io.IOException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import java.io.ByteArrayOutputStream;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
class JDTCompiler$1 implements INameEnvironment {
    final   String val$targetClassName;
    final   String val$sourceFile;
    final   ClassLoader val$classLoader;
    public NameEnvironmentAnswer findType ( final char[][] compoundTypeName ) {
        final StringBuilder result = new StringBuilder();
        for ( int i = 0; i < compoundTypeName.length; ++i ) {
            if ( i > 0 ) {
                result.append ( '.' );
            }
            result.append ( compoundTypeName[i] );
        }
        return this.findType ( result.toString() );
    }
    public NameEnvironmentAnswer findType ( final char[] typeName, final char[][] packageName ) {
        final StringBuilder result = new StringBuilder();
        int i;
        for ( i = 0; i < packageName.length; ++i ) {
            if ( i > 0 ) {
                result.append ( '.' );
            }
            result.append ( packageName[i] );
        }
        if ( i > 0 ) {
            result.append ( '.' );
        }
        result.append ( typeName );
        return this.findType ( result.toString() );
    }
    private NameEnvironmentAnswer findType ( final String className ) {
        if ( className.equals ( this.val$targetClassName ) ) {
            final ICompilationUnit compilationUnit = ( ICompilationUnit ) new CompilationUnit ( this.val$sourceFile, className );
            return new NameEnvironmentAnswer ( compilationUnit, ( AccessRestriction ) null );
        }
        final String resourceName = className.replace ( '.', '/' ) + ".class";
        try ( final InputStream is = this.val$classLoader.getResourceAsStream ( resourceName ) ) {
            if ( is != null ) {
                final byte[] buf = new byte[8192];
                final ByteArrayOutputStream baos = new ByteArrayOutputStream ( buf.length );
                int count;
                while ( ( count = is.read ( buf, 0, buf.length ) ) > 0 ) {
                    baos.write ( buf, 0, count );
                }
                baos.flush();
                final byte[] classBytes = baos.toByteArray();
                final char[] fileName = className.toCharArray();
                final ClassFileReader classFileReader = new ClassFileReader ( classBytes, fileName, true );
                return new NameEnvironmentAnswer ( ( IBinaryType ) classFileReader, ( AccessRestriction ) null );
            }
        } catch ( IOException exc ) {
            JDTCompiler.access$000 ( JDTCompiler.this ).error ( "Compilation error", exc );
        } catch ( ClassFormatException exc2 ) {
            JDTCompiler.access$000 ( JDTCompiler.this ).error ( "Compilation error", ( Throwable ) exc2 );
        }
        return null;
    }
    private boolean isPackage ( final String result ) {
        if ( result.equals ( this.val$targetClassName ) ) {
            return false;
        }
        final String resourceName = result.replace ( '.', '/' ) + ".class";
        try ( final InputStream is = this.val$classLoader.getResourceAsStream ( resourceName ) ) {
            return is == null;
        } catch ( IOException e ) {
            return false;
        }
    }
    public boolean isPackage ( final char[][] parentPackageName, final char[] packageName ) {
        final StringBuilder result = new StringBuilder();
        int i = 0;
        if ( parentPackageName != null ) {
            while ( i < parentPackageName.length ) {
                if ( i > 0 ) {
                    result.append ( '.' );
                }
                result.append ( parentPackageName[i] );
                ++i;
            }
        }
        if ( Character.isUpperCase ( packageName[0] ) && !this.isPackage ( result.toString() ) ) {
            return false;
        }
        if ( i > 0 ) {
            result.append ( '.' );
        }
        result.append ( packageName );
        return this.isPackage ( result.toString() );
    }
    public void cleanup() {
    }
}
