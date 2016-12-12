package org.apache.catalina.core;
import java.util.Map;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.apache.tomcat.util.http.fileupload.ParameterParser;
import java.util.Locale;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Collection;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import java.io.IOException;
import java.io.File;
import org.apache.tomcat.util.http.fileupload.FileItem;
import javax.servlet.http.Part;
public class ApplicationPart implements Part {
    private final FileItem fileItem;
    private final File location;
    public ApplicationPart ( final FileItem fileItem, final File location ) {
        this.fileItem = fileItem;
        this.location = location;
    }
    public void delete() throws IOException {
        this.fileItem.delete();
    }
    public String getContentType() {
        return this.fileItem.getContentType();
    }
    public String getHeader ( final String name ) {
        if ( this.fileItem instanceof DiskFileItem ) {
            return ( ( DiskFileItem ) this.fileItem ).getHeaders().getHeader ( name );
        }
        return null;
    }
    public Collection<String> getHeaderNames() {
        if ( this.fileItem instanceof DiskFileItem ) {
            final LinkedHashSet<String> headerNames = new LinkedHashSet<String>();
            final Iterator<String> iter = ( ( DiskFileItem ) this.fileItem ).getHeaders().getHeaderNames();
            while ( iter.hasNext() ) {
                headerNames.add ( iter.next() );
            }
            return headerNames;
        }
        return ( Collection<String> ) Collections.emptyList();
    }
    public Collection<String> getHeaders ( final String name ) {
        if ( this.fileItem instanceof DiskFileItem ) {
            final LinkedHashSet<String> headers = new LinkedHashSet<String>();
            final Iterator<String> iter = ( ( DiskFileItem ) this.fileItem ).getHeaders().getHeaders ( name );
            while ( iter.hasNext() ) {
                headers.add ( iter.next() );
            }
            return headers;
        }
        return ( Collection<String> ) Collections.emptyList();
    }
    public InputStream getInputStream() throws IOException {
        return this.fileItem.getInputStream();
    }
    public String getName() {
        return this.fileItem.getFieldName();
    }
    public long getSize() {
        return this.fileItem.getSize();
    }
    public void write ( final String fileName ) throws IOException {
        File file = new File ( fileName );
        if ( !file.isAbsolute() ) {
            file = new File ( this.location, fileName );
        }
        try {
            this.fileItem.write ( file );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    public String getString ( final String encoding ) throws UnsupportedEncodingException {
        return this.fileItem.getString ( encoding );
    }
    public String getSubmittedFileName() {
        String fileName = null;
        final String cd = this.getHeader ( "Content-Disposition" );
        if ( cd != null ) {
            final String cdl = cd.toLowerCase ( Locale.ENGLISH );
            if ( cdl.startsWith ( "form-data" ) || cdl.startsWith ( "attachment" ) ) {
                final ParameterParser paramParser = new ParameterParser();
                paramParser.setLowerCaseNames ( true );
                final Map<String, String> params = paramParser.parse ( cd, ';' );
                if ( params.containsKey ( "filename" ) ) {
                    fileName = params.get ( "filename" );
                    if ( fileName != null ) {
                        if ( fileName.indexOf ( 92 ) > -1 ) {
                            fileName = HttpParser.unquote ( fileName.trim() );
                        } else {
                            fileName = fileName.trim();
                        }
                    } else {
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }
}
