package org.apache.tomcat.util.http.fileupload;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
public interface FileItem extends FileItemHeadersSupport {
    InputStream getInputStream() throws IOException;
    String getContentType();
    String getName();
    boolean isInMemory();
    long getSize();
    byte[] get();
    String getString ( String encoding ) throws UnsupportedEncodingException;
    String getString();
    void write ( File file ) throws Exception;
    void delete();
    String getFieldName();
    void setFieldName ( String name );
    boolean isFormField();
    void setFormField ( boolean state );
    OutputStream getOutputStream() throws IOException;
}
