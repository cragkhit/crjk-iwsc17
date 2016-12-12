package org.apache.jasper.compiler;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.jsp.tagext.FunctionInfo;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
public class ELFunctionMapper {
    private int currFunc = 0;
    private StringBuilder ds;
    private StringBuilder ss;
    public static void map ( Node.Nodes page )
    throws JasperException {
        ELFunctionMapper map = new ELFunctionMapper();
        map.ds = new StringBuilder();
        map.ss = new StringBuilder();
        page.visit ( map.new ELFunctionVisitor() );
        String ds = map.ds.toString();
        if ( ds.length() > 0 ) {
            Node root = page.getRoot();
            @SuppressWarnings ( "unused" )
            Node unused = new Node.Declaration ( map.ss.toString(), null, root );
            unused = new Node.Declaration (
                "static {\n" + ds + "}\n", null, root );
        }
    }
    private class ELFunctionVisitor extends Node.Visitor {
        private final HashMap<String, String> gMap = new HashMap<>();
        @Override
        public void visit ( Node.ParamAction n ) throws JasperException {
            doMap ( n.getValue() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.IncludeAction n ) throws JasperException {
            doMap ( n.getPage() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.ForwardAction n ) throws JasperException {
            doMap ( n.getPage() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.SetProperty n ) throws JasperException {
            doMap ( n.getValue() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.UseBean n ) throws JasperException {
            doMap ( n.getBeanName() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.PlugIn n ) throws JasperException {
            doMap ( n.getHeight() );
            doMap ( n.getWidth() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.JspElement n ) throws JasperException {
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                doMap ( attrs[i] );
            }
            doMap ( n.getNameAttribute() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.UninterpretedTag n ) throws JasperException {
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                doMap ( attrs[i] );
            }
            visitBody ( n );
        }
        @Override
        public void visit ( Node.CustomTag n ) throws JasperException {
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                doMap ( attrs[i] );
            }
            visitBody ( n );
        }
        @Override
        public void visit ( Node.ELExpression n ) throws JasperException {
            doMap ( n.getEL() );
        }
        private void doMap ( Node.JspAttribute attr )
        throws JasperException {
            if ( attr != null ) {
                doMap ( attr.getEL() );
            }
        }
        private void doMap ( ELNode.Nodes el )
        throws JasperException {
            class Fvisitor extends ELNode.Visitor {
                private final List<ELNode.Function> funcs = new ArrayList<>();
                private final Set<String> keySet = new HashSet<>();
                @Override
                public void visit ( ELNode.Function n ) throws JasperException {
                    String key = n.getPrefix() + ":" + n.getName();
                    if ( keySet.add ( key ) ) {
                        funcs.add ( n );
                    }
                }
            }
            if ( el == null ) {
                return;
            }
            Fvisitor fv = new Fvisitor();
            el.visit ( fv );
            List<ELNode.Function> functions = fv.funcs;
            if ( functions.size() == 0 ) {
                return;
            }
            String decName = matchMap ( functions );
            if ( decName != null ) {
                el.setMapName ( decName );
                return;
            }
            decName = getMapName();
            ss.append ( "private static org.apache.jasper.runtime.ProtectedFunctionMapper " + decName + ";\n" );
            ds.append ( "  " + decName + "= " );
            ds.append ( "org.apache.jasper.runtime.ProtectedFunctionMapper" );
            String funcMethod = null;
            if ( functions.size() == 1 ) {
                funcMethod = ".getMapForFunction";
            } else {
                ds.append ( ".getInstance();\n" );
                funcMethod = "  " + decName + ".mapFunction";
            }
            for ( ELNode.Function f : functions ) {
                FunctionInfo funcInfo = f.getFunctionInfo();
                String fnQName = f.getPrefix() + ":" + f.getName();
                if ( funcInfo == null ) {
                    ds.append ( funcMethod + "(null, null, null, null);\n" );
                } else {
                    ds.append ( funcMethod + "(\"" + fnQName + "\", " +
                                getCanonicalName ( funcInfo.getFunctionClass() ) +
                                ".class, " + '\"' + f.getMethodName() + "\", " +
                                "new Class[] {" );
                    String params[] = f.getParameters();
                    for ( int k = 0; k < params.length; k++ ) {
                        if ( k != 0 ) {
                            ds.append ( ", " );
                        }
                        int iArray = params[k].indexOf ( '[' );
                        if ( iArray < 0 ) {
                            ds.append ( params[k] + ".class" );
                        } else {
                            String baseType = params[k].substring ( 0, iArray );
                            ds.append ( "java.lang.reflect.Array.newInstance(" );
                            ds.append ( baseType );
                            ds.append ( ".class," );
                            int aCount = 0;
                            for ( int jj = iArray; jj < params[k].length(); jj++ ) {
                                if ( params[k].charAt ( jj ) == '[' ) {
                                    aCount++;
                                }
                            }
                            if ( aCount == 1 ) {
                                ds.append ( "0).getClass()" );
                            } else {
                                ds.append ( "new int[" + aCount + "]).getClass()" );
                            }
                        }
                    }
                    ds.append ( "});\n" );
                }
                gMap.put ( fnQName + ':' + f.getUri(), decName );
            }
            el.setMapName ( decName );
        }
        private String matchMap ( List<ELNode.Function> functions ) {
            String mapName = null;
            for ( ELNode.Function f : functions ) {
                String temName = gMap.get ( f.getPrefix() + ':' + f.getName() +
                                            ':' + f.getUri() );
                if ( temName == null ) {
                    return null;
                }
                if ( mapName == null ) {
                    mapName = temName;
                } else if ( !temName.equals ( mapName ) ) {
                    return null;
                }
            }
            return mapName;
        }
        private String getMapName() {
            return "_jspx_fnmap_" + currFunc++;
        }
        private String getCanonicalName ( String className ) throws JasperException {
            Class<?> clazz;
            ClassLoader tccl;
            if ( Constants.IS_SECURITY_ENABLED ) {
                PrivilegedAction<ClassLoader> pa = new PrivilegedGetTccl();
                tccl = AccessController.doPrivileged ( pa );
            } else {
                tccl = Thread.currentThread().getContextClassLoader();
            }
            try {
                clazz = Class.forName ( className, false, tccl );
            } catch ( ClassNotFoundException e ) {
                throw new JasperException ( e );
            }
            return clazz.getCanonicalName();
        }
    }
}
