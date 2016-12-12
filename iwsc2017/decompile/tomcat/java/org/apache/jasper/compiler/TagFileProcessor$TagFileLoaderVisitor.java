package org.apache.jasper.compiler;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.jsp.tagext.TagFileInfo;
import java.io.IOException;
import org.apache.jasper.JasperException;
private class TagFileLoaderVisitor extends Node.Visitor {
    private Compiler compiler;
    private PageInfo pageInfo;
    TagFileLoaderVisitor ( final Compiler compiler ) {
        this.compiler = compiler;
        this.pageInfo = compiler.getPageInfo();
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        final TagFileInfo tagFileInfo = n.getTagFileInfo();
        if ( tagFileInfo != null ) {
            final String tagFilePath = tagFileInfo.getPath();
            if ( tagFilePath.startsWith ( "/META-INF/" ) ) {
                final TldResourcePath tldResourcePath = this.compiler.getCompilationContext().getTldResourcePath ( tagFileInfo.getTagInfo().getTagLibrary().getURI() );
                try ( final Jar jar = tldResourcePath.openJar() ) {
                    if ( jar != null ) {
                        this.pageInfo.addDependant ( jar.getURL ( tldResourcePath.getEntryName() ), jar.getLastModified ( tldResourcePath.getEntryName() ) );
                        this.pageInfo.addDependant ( jar.getURL ( tagFilePath.substring ( 1 ) ), jar.getLastModified ( tagFilePath.substring ( 1 ) ) );
                    } else {
                        this.pageInfo.addDependant ( tagFilePath, this.compiler.getCompilationContext().getLastModified ( tagFilePath ) );
                    }
                } catch ( IOException ioe ) {
                    throw new JasperException ( ioe );
                }
            } else {
                this.pageInfo.addDependant ( tagFilePath, this.compiler.getCompilationContext().getLastModified ( tagFilePath ) );
            }
            final Class<?> c = ( Class<?> ) TagFileProcessor.access$000 ( TagFileProcessor.this, this.compiler, tagFilePath, n.getTagInfo(), this.pageInfo );
            n.setTagHandlerClass ( c );
        }
        this.visitBody ( n );
    }
}
