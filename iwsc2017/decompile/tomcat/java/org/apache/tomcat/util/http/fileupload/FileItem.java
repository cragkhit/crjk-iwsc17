package org.apache.tomcat.util.http.fileupload;
import java.io.OutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
public interface FileItem extends FileItemHeadersSupport {
    InputStream getInputStream() throws IOException;
    String getContentType();
    String getName();
    boolean isInMemory();
    long getSize();
    byte[] get();
    String getString ( String p0 ) throws UnsupportedEncodingException;
    String getString();
    void write ( File p0 ) throws Exception;
    void delete();
    String getFieldName();
    void setFieldName ( String p0 );
    boolean isFormField();
    void setFormField ( boolean p0 );
    OutputStream getOutputStream() throws IOException;
}
