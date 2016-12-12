package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.FileNotFoundException;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import java.io.File;
import java.util.Map;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.core.compiler.IProblem;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import org.apache.jasper.JasperException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import java.util.Locale;
import java.util.HashMap;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import java.io.ByteArrayOutputStream;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public class JDTCompiler extends Compiler {
    private final Log log;
    public JDTCompiler() {
        this.log = LogFactory.getLog ( JDTCompiler.class );
    }
    @Override
    protected void generateClass ( final String[] smap ) throws FileNotFoundException, JasperException, Exception {
        long t1 = 0L;
        if ( this.log.isDebugEnabled() ) {
            t1 = System.currentTimeMillis();
        }
        final String sourceFile = this.ctxt.getServletJavaFileName();
        final String outputDir = this.ctxt.getOptions().getScratchDir().getAbsolutePath();
        final String packageName = this.ctxt.getServletPackageName();
        final String targetClassName = ( ( packageName.length() != 0 ) ? ( packageName + "." ) : "" ) + this.ctxt.getServletClassName();
        final ClassLoader classLoader = this.ctxt.getJspLoader();
        final String[] fileNames = { sourceFile };
        final String[] classNames = { targetClassName };
        final ArrayList<JavacErrorDetail> problemList = new ArrayList<JavacErrorDetail>();
        final INameEnvironment env = ( INameEnvironment ) new INameEnvironment() {
            final   String val$sourceFile;
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
                if ( className.equals ( targetClassName ) ) {
                    final ICompilationUnit compilationUnit = ( ICompilationUnit ) new CompilationUnit();
                    return new NameEnvironmentAnswer ( compilationUnit, ( AccessRestriction ) null );
                }
                final String resourceName = className.replace ( '.', '/' ) + ".class";
                try ( final InputStream is = classLoader.getResourceAsStream ( resourceName ) ) {
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
                    JDTCompiler.this.log.error ( "Compilation error", exc );
                } catch ( ClassFormatException exc2 ) {
                    JDTCompiler.this.log.error ( "Compilation error", ( Throwable ) exc2 );
                }
                return null;
            }
            private boolean isPackage ( final String result ) {
                if ( result.equals ( targetClassName ) ) {
                    return false;
                }
                final String resourceName = result.replace ( '.', '/' ) + ".class";
                try ( final InputStream is = classLoader.getResourceAsStream ( resourceName ) ) {
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
        };
        final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
        final Map<String, String> settings = new HashMap<String, String>();
        settings.put ( "org.eclipse.jdt.core.compiler.debug.lineNumber", "generate" );
        settings.put ( "org.eclipse.jdt.core.compiler.debug.sourceFile", "generate" );
        settings.put ( "org.eclipse.jdt.core.compiler.problem.deprecation", "ignore" );
        if ( this.ctxt.getOptions().getJavaEncoding() != null ) {
            settings.put ( "org.eclipse.jdt.core.encoding", this.ctxt.getOptions().getJavaEncoding() );
        }
        if ( this.ctxt.getOptions().getClassDebugInfo() ) {
            settings.put ( "org.eclipse.jdt.core.compiler.debug.localVariable", "generate" );
        }
        if ( this.ctxt.getOptions().getCompilerSourceVM() != null ) {
            final String opt = this.ctxt.getOptions().getCompilerSourceVM();
            if ( opt.equals ( "1.1" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.1" );
            } else if ( opt.equals ( "1.2" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.2" );
            } else if ( opt.equals ( "1.3" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.3" );
            } else if ( opt.equals ( "1.4" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.4" );
            } else if ( opt.equals ( "1.5" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.5" );
            } else if ( opt.equals ( "1.6" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.6" );
            } else if ( opt.equals ( "1.7" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.7" );
            } else if ( opt.equals ( "1.8" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.8" );
            } else if ( opt.equals ( "1.9" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.9" );
            } else {
                this.log.warn ( "Unknown source VM " + opt + " ignored." );
                settings.put ( "org.eclipse.jdt.core.compiler.source", "1.8" );
            }
        } else {
            settings.put ( "org.eclipse.jdt.core.compiler.source", "1.8" );
        }
        if ( this.ctxt.getOptions().getCompilerTargetVM() != null ) {
            final String opt = this.ctxt.getOptions().getCompilerTargetVM();
            if ( opt.equals ( "1.1" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.1" );
            } else if ( opt.equals ( "1.2" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.2" );
            } else if ( opt.equals ( "1.3" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.3" );
            } else if ( opt.equals ( "1.4" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4" );
            } else if ( opt.equals ( "1.5" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5" );
                settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.5" );
            } else if ( opt.equals ( "1.6" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6" );
                settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.6" );
            } else if ( opt.equals ( "1.7" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7" );
                settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.7" );
            } else if ( opt.equals ( "1.8" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8" );
                settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.8" );
            } else if ( opt.equals ( "1.9" ) ) {
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.9" );
                settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.9" );
            } else {
                this.log.warn ( "Unknown target VM " + opt + " ignored." );
                settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8" );
            }
        } else {
            settings.put ( "org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8" );
            settings.put ( "org.eclipse.jdt.core.compiler.compliance", "1.8" );
        }
        final IProblemFactory problemFactory = ( IProblemFactory ) new DefaultProblemFactory ( Locale.getDefault() );
        final ICompilerRequestor requestor = ( ICompilerRequestor ) new ICompilerRequestor() {
            public void acceptResult ( final CompilationResult result ) {
                try {
                    if ( result.hasProblems() ) {
                        final IProblem[] problems = ( IProblem[] ) result.getProblems();
                        for ( int i = 0; i < problems.length; ++i ) {
                            final IProblem problem = problems[i];
                            if ( problem.isError() ) {
                                final String name = new String ( problems[i].getOriginatingFileName() );
                                try {
                                    problemList.add ( ErrorDispatcher.createJavacError ( name, JDTCompiler.this.pageNodes, new StringBuilder ( problem.getMessage() ), problem.getSourceLineNumber(), JDTCompiler.this.ctxt ) );
                                } catch ( JasperException e ) {
                                    JDTCompiler.this.log.error ( "Error visiting node", ( Throwable ) e );
                                }
                            }
                        }
                    }
                    if ( problemList.isEmpty() ) {
                        final ClassFile[] classFiles = result.getClassFiles();
                        for ( int i = 0; i < classFiles.length; ++i ) {
                            final ClassFile classFile = classFiles[i];
                            final char[][] compoundName = classFile.getCompoundName();
                            final StringBuilder classFileName = new StringBuilder ( outputDir ).append ( '/' );
                            for ( int j = 0; j < compoundName.length; ++j ) {
                                if ( j > 0 ) {
                                    classFileName.append ( '/' );
                                }
                                classFileName.append ( compoundName[j] );
                            }
                            final byte[] bytes = classFile.getBytes();
                            classFileName.append ( ".class" );
                            try ( final FileOutputStream fout = new FileOutputStream ( classFileName.toString() );
                                        final BufferedOutputStream bos = new BufferedOutputStream ( fout ) ) {
                                bos.write ( bytes );
                            }
                        }
                    }
                } catch ( IOException exc ) {
                    JDTCompiler.this.log.error ( "Compilation error", exc );
                }
            }
        };
        final ICompilationUnit[] compilationUnits = new ICompilationUnit[classNames.length];
        for ( int i = 0; i < compilationUnits.length; ++i ) {
            final String className = classNames[i];
            class CompilationUnit implements ICompilationUnit {
                private final String className = className;
                private final String sourceFile = this.val$sourceFile;
                final   JDTCompiler this$0 = JDTCompiler.this;
                CompilationUnit ( final String sourceFile, final String className ) {
                }
                public char[] getFileName() {
                    return this.sourceFile.toCharArray();
                }
                public char[] getContents() {
                    char[] result = null;
                    try ( final FileInputStream is = new FileInputStream ( this.sourceFile );
                                final InputStreamReader isr = new InputStreamReader ( is, this.this$0.ctxt.getOptions().getJavaEncoding() );
                                final Reader reader = new BufferedReader ( isr ) ) {
                        final char[] chars = new char[8192];
                        final StringBuilder buf = new StringBuilder();
                        int count;
                        while ( ( count = reader.read ( chars, 0, chars.length ) ) > 0 ) {
                            buf.append ( chars, 0, count );
                        }
                        result = new char[buf.length()];
                        buf.getChars ( 0, result.length, result, 0 );
                    } catch ( IOException e ) {
                        this.this$0.log.error ( "Compilation error", e );
                    }
                    return result;
                }
                public char[] getMainTypeName() {
                    final int dot = this.className.lastIndexOf ( 46 );
                    if ( dot > 0 ) {
                        return this.className.substring ( dot + 1 ).toCharArray();
                    }
                    return this.className.toCharArray();
                }
                public char[][] getPackageName() {
                    final StringTokenizer izer = new StringTokenizer ( this.className, "." );
                    final char[][] result = new char[izer.countTokens() - 1][];
                    for ( int i = 0; i < result.length; ++i ) {
                        final String tok = izer.nextToken();
                        result[i] = tok.toCharArray();
                    }
                    return result;
                }
                public boolean ignoreOptionalProblems() {
                    return false;
                }
            }
            compilationUnits[i] = ( ICompilationUnit ) new CompilationUnit();
        }
        final CompilerOptions cOptions = new CompilerOptions ( ( Map ) settings );
        cOptions.parseLiteralExpressionsAsConstants = true;
        final org.eclipse.jdt.internal.compiler.Compiler compiler = new org.eclipse.jdt.internal.compiler.Compiler ( env, policy, cOptions, requestor, problemFactory );
        compiler.compile ( compilationUnits );
        if ( !this.ctxt.keepGenerated() ) {
            final File javaFile = new File ( this.ctxt.getServletJavaFileName() );
            javaFile.delete();
        }
        if ( !problemList.isEmpty() ) {
            final JavacErrorDetail[] jeds = problemList.toArray ( new JavacErrorDetail[0] );
            this.errDispatcher.javacError ( jeds );
        }
        if ( this.log.isDebugEnabled() ) {
            final long t2 = System.currentTimeMillis();
            this.log.debug ( "Compiled " + this.ctxt.getServletJavaFileName() + " " + ( t2 - t1 ) + "ms" );
        }
        if ( this.ctxt.isPrototypeMode() ) {
            return;
        }
        if ( !this.options.isSmapSuppressed() ) {
            SmapUtil.installSmap ( smap );
        }
    }
}
