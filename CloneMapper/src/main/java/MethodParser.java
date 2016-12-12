import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
public class MethodParser {
    protected ArrayList<Method> methodList = new ArrayList<>();
    protected String FILE_PATH = "";
    protected String PREFIX_TO_REMOVE = "";
    public MethodParser() {
    }
    public MethodParser ( String filePath, String prefixToRemove ) {
        FILE_PATH = filePath;
        PREFIX_TO_REMOVE = prefixToRemove;
    }
    public ArrayList<Method> parseMethods() {
        try {
            FileInputStream in = new FileInputStream ( FILE_PATH );
            CompilationUnit cu;
            try {
                cu = JavaParser.parse ( in );
                new MethodVisitor().visit ( cu, null );
                new ConstructorVisitor().visit ( cu, null );
            } catch ( Throwable e ) {
                System.out.println ( "ERROR: unparsed file: " + FILE_PATH );
                String content = new Scanner ( new File ( FILE_PATH ) ).useDelimiter ( "\\Z" ).next();
                Method m = new Method ( FILE_PATH.replace ( PREFIX_TO_REMOVE, "" ), "method", content, -1, -1, new LinkedList<Parameter>() );
                methodList.add ( m );
            } finally {
                in.close();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return methodList;
    }
    private String getOnlyMethodName ( String methodHeader ) {
        String[] methodNames = methodHeader.split ( " " );
        String methodName = "no_name";
        for ( String mName : methodNames ) {
            if ( mName.contains ( "(" ) ) {
                methodName = mName.substring ( 0, mName.indexOf ( "(" ) );
            }
        }
        return methodName;
    }
    private class MethodVisitor extends VoidVisitorAdapter {
        @Override
        public void visit ( MethodDeclaration n, Object arg ) {
            String mthd = n.getDeclarationAsString();
            Method m = new Method ( FILE_PATH.replace ( PREFIX_TO_REMOVE, "" ), getOnlyMethodName ( n.getDeclarationAsString() )
                                    , mthd
                                    , n.getBeginLine()
                                    , n.getEndLine()
                                    , n.getParameters() );
            methodList.add ( m );
            super.visit ( n, arg );
        }
    }
    private class ConstructorVisitor extends VoidVisitorAdapter {
        @Override
        public void visit ( ConstructorDeclaration c, Object arg ) {
            String cons = c.getDeclarationAsString() + c.getBlock();
            Method m = new Method ( FILE_PATH.replace ( PREFIX_TO_REMOVE, "" ), getOnlyMethodName ( c.getDeclarationAsString() )
                                    , cons
                                    , c.getBeginLine()
                                    , c.getEndLine()
                                    , c.getParameters() );
            methodList.add ( m );
            super.visit ( c, arg );
        }
    }
    public void printMethods ( String javaFile ) throws IOException {
        FileInputStream in = new FileInputStream ( javaFile );
        CompilationUnit cu;
        try {
            cu = JavaParser.parse ( in );
            new MethodVisitor().visit ( cu, null );
            new ConstructorVisitor().visit ( cu, null );
        } catch ( ParseException e ) {
            e.printStackTrace();
        } finally {
            in.close();
        }
    }
}
