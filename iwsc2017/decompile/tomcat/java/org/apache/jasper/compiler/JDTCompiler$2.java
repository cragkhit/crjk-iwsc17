package org.apache.jasper.compiler;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.core.compiler.IProblem;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import org.apache.jasper.JasperException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import java.util.ArrayList;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
class JDTCompiler$2 implements ICompilerRequestor {
    final   ArrayList val$problemList;
    final   String val$outputDir;
    public void acceptResult ( final CompilationResult result ) {
        try {
            if ( result.hasProblems() ) {
                final IProblem[] problems = ( IProblem[] ) result.getProblems();
                for ( int i = 0; i < problems.length; ++i ) {
                    final IProblem problem = problems[i];
                    if ( problem.isError() ) {
                        final String name = new String ( problems[i].getOriginatingFileName() );
                        try {
                            this.val$problemList.add ( ErrorDispatcher.createJavacError ( name, JDTCompiler.this.pageNodes, new StringBuilder ( problem.getMessage() ), problem.getSourceLineNumber(), JDTCompiler.this.ctxt ) );
                        } catch ( JasperException e ) {
                            JDTCompiler.access$000 ( JDTCompiler.this ).error ( "Error visiting node", ( Throwable ) e );
                        }
                    }
                }
            }
            if ( this.val$problemList.isEmpty() ) {
                final ClassFile[] classFiles = result.getClassFiles();
                for ( int i = 0; i < classFiles.length; ++i ) {
                    final ClassFile classFile = classFiles[i];
                    final char[][] compoundName = classFile.getCompoundName();
                    final StringBuilder classFileName = new StringBuilder ( this.val$outputDir ).append ( '/' );
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
            JDTCompiler.access$000 ( JDTCompiler.this ).error ( "Compilation error", exc );
        }
    }
}
