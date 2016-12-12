package org.apache.tomcat.util.http.fileupload;
public interface FileItemFactory {
    FileItem createItem (
        String fieldName,
        String contentType,
        boolean isFormField,
        String fileName
    );
}
