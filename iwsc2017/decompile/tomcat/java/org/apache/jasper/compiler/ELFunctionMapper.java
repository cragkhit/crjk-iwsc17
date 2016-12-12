package org.apache.jasper.compiler;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.jasper.Constants;
import javax.servlet.jsp.tagext.FunctionInfo;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import org.apache.jasper.JasperException;
public class ELFunctionMapper {
    private int currFunc;
    private StringBuilder ds;
    private StringBuilder ss;
    public ELFunctionMapper() {
        this.currFunc = 0;
    }
    public static void map ( final Node.Nodes page ) throws JasperException {
        final ELFunctionMapper map = new ELFunctionMapper();
        map.ds = new StringBuilder();
        map.ss = new StringBuilder();
        page.visit ( new ELFunctionVisitor() );
        final String ds = map.ds.toString();
        if ( ds.length() > 0 ) {
            final Node root = page.getRoot();
            Node unused = new Node.Declaration ( map.ss.toString(), null, root );
            unused = new Node.Declaration ( "static {\n" + ds + "}\n", null, root );
        }
    }
    private class ELFunctionVisitor extends Node.Visitor {
        private final HashMap<String, String> gMap;
        private ELFunctionVisitor() {
            this.gMap = new HashMap<String, String>();
        }
        @Override
        public void visit ( final Node.ParamAction n ) throws JasperException {
            this.doMap ( n.getValue() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.IncludeAction n ) throws JasperException {
            this.doMap ( n.getPage() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.ForwardAction n ) throws JasperException {
            this.doMap ( n.getPage() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.SetProperty n ) throws JasperException {
            this.doMap ( n.getValue() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.UseBean n ) throws JasperException {
            this.doMap ( n.getBeanName() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.PlugIn n ) throws JasperException {
            this.doMap ( n.getHeight() );
            this.doMap ( n.getWidth() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.JspElement n ) throws JasperException {
            final Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
                this.doMap ( attrs[i] );
            }
            this.doMap ( n.getNameAttribute() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.UninterpretedTag n ) throws JasperException {
            final Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
                this.doMap ( attrs[i] );
            }
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.CustomTag n ) throws JasperException {
            final Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
                this.doMap ( attrs[i] );
            }
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.ELExpression n ) throws JasperException {
            this.doMap ( n.getEL() );
        }
        private void doMap ( final Node.JspAttribute attr ) throws JasperException {
            if ( attr != null ) {
                this.doMap ( attr.getEL() );
            }
        }
        private void doMap ( final ELNode.Nodes el ) throws JasperException {
            if ( el == null ) {
                return;
            }
            class Fvisitor extends ELNode.Visitor {
                private final List<ELNode.Function> funcs;
                private final Set<String> keySet;
                Fvisitor() {
                    this.funcs = new ArrayList<ELNode.Function>();
                    this.keySet = new HashSet<String>();
                }
                @Override
                public void visit ( final ELNode.Function n ) throws JasperException {
                    final String key = n.getPrefix() + ":" + n.getName();
                    if ( this.keySet.add ( key ) ) {
                        this.funcs.add ( n );
                    }
                }
            }
            final Fvisitor fv = new Fvisitor();
            el.visit ( fv );
            final List<ELNode.Function> functions = fv.funcs;
            if ( functions.size() == 0 ) {
                return;
            }
            String decName = this.matchMap ( functions );
            if ( decName != null ) {
                el.setMapName ( decName );
                return;
            }
            decName = this.getMapName();
            ELFunctionMapper.this.ss.append ( "private static org.apache.jasper.runtime.ProtectedFunctionMapper " + decName + ";\n" );
            ELFunctionMapper.this.ds.append ( "  " + decName + "= " );
            ELFunctionMapper.this.ds.append ( "org.apache.jasper.runtime.ProtectedFunctionMapper" );
            String funcMethod = null;
            if ( functions.size() == 1 ) {
                funcMethod = ".getMapForFunction";
            } else {
                ELFunctionMapper.this.ds.append ( ".getInstance();\n" );
                funcMethod = "  " + decName + ".mapFunction";
            }
            for ( final ELNode.Function f : functions ) {
                final FunctionInfo funcInfo = f.getFunctionInfo();
                final String fnQName = f.getPrefix() + ":" + f.getName();
                if ( funcInfo == null ) {
                    ELFunctionMapper.this.ds.append ( funcMethod + "(null, null, null, null);\n" );
                } else {
                    ELFunctionMapper.this.ds.append ( funcMethod + "(\"" + fnQName + "\", " + this.getCanonicalName ( funcInfo.getFunctionClass() ) + ".class, " + '\"' + f.getMethodName() + "\", new Class[] {" );
                    final String[] params = f.getParameters();
                    for ( int k = 0; k < params.length; ++k ) {
                        if ( k != 0 ) {
                            ELFunctionMapper.this.ds.append ( ", " );
                        }
                        final int iArray = params[k].indexOf ( 91 );
                        if ( iArray < 0 ) {
                            ELFunctionMapper.this.ds.append ( params[k] + ".class" );
                        } else {
                            final String baseType = params[k].substring ( 0, iArray );
                            ELFunctionMapper.this.ds.append ( "java.lang.reflect.Array.newInstance(" );
                            ELFunctionMapper.this.ds.append ( baseType );
                            ELFunctionMapper.this.ds.append ( ".class," );
                            int aCount = 0;
                            for ( int jj = iArray; jj < params[k].length(); ++jj ) {
                                if ( params[k].charAt ( jj ) == '[' ) {
                                    ++aCount;
                                }
                            }
                            if ( aCount == 1 ) {
                                ELFunctionMapper.this.ds.append ( "0).getClass()" );
                            } else {
                                ELFunctionMapper.this.ds.append ( "new int[" + aCount + "]).getClass()" );
                            }
                        }
                    }
                    ELFunctionMapper.this.ds.append ( "});\n" );
                }
                this.gMap.put ( fnQName + ':' + f.getUri(), decName );
            }
            el.setMapName ( decName );
        }
        private String matchMap ( final List<ELNode.Function> functions ) {
            String mapName = null;
            for ( final ELNode.Function f : functions ) {
                final String temName = this.gMap.get ( f.getPrefix() + ':' + f.getName() + ':' + f.getUri() );
                if ( temName == null ) {
                    return null;
                }
                if ( mapName == null ) {
                    mapName = temName;
                } else {
                    if ( !temName.equals ( mapName ) ) {
                        return null;
                    }
                    continue;
                }
            }
            return mapName;
        }
        private String getMapName() {
            return "_jspx_fnmap_" + ELFunctionMapper.this.currFunc++;
        }
        private String getCanonicalName ( final String className ) throws JasperException {
            ClassLoader tccl;
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedAction<ClassLoader> pa = new PrivilegedGetTccl();
                tccl = AccessController.doPrivileged ( pa );
            } else {
                tccl = Thread.currentThread().getContextClassLoader();
            }
            Class<?> clazz;
            try {
                clazz = Class.forName ( className, false, tccl );
            } catch ( ClassNotFoundException e ) {
                throw new JasperException ( e );
            }
            return clazz.getCanonicalName();
        }
    }
}
