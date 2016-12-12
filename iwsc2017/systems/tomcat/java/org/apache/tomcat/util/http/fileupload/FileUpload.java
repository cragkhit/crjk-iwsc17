package org.apache.tomcat.util.http.fileupload;
public class FileUpload
    extends FileUploadBase {
    private FileItemFactory fileItemFactory;
    public FileUpload() {
        super();
    }
    public FileUpload ( FileItemFactory fileItemFactory ) {
        super();
        this.fileItemFactory = fileItemFactory;
    }
    @Override
    public FileItemFactory getFileItemFactory() {
        return fileItemFactory;
    }
    @Override
    public void setFileItemFactory ( FileItemFactory factory ) {
        this.fileItemFactory = factory;
    }
}
