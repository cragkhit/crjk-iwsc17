package javax.servlet;
import javax.servlet.annotation.MultipartConfig;
public class MultipartConfigElement {
    private final String location;
    private final long maxFileSize;
    private final long maxRequestSize;
    private final int fileSizeThreshold;
    public MultipartConfigElement ( final String location ) {
        if ( location != null ) {
            this.location = location;
        } else {
            this.location = "";
        }
        this.maxFileSize = -1L;
        this.maxRequestSize = -1L;
        this.fileSizeThreshold = 0;
    }
    public MultipartConfigElement ( final String location, final long maxFileSize, final long maxRequestSize, final int fileSizeThreshold ) {
        if ( location != null ) {
            this.location = location;
        } else {
            this.location = "";
        }
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
        if ( fileSizeThreshold > 0 ) {
            this.fileSizeThreshold = fileSizeThreshold;
        } else {
            this.fileSizeThreshold = 0;
        }
    }
    public MultipartConfigElement ( final MultipartConfig annotation ) {
        this.location = annotation.location();
        this.maxFileSize = annotation.maxFileSize();
        this.maxRequestSize = annotation.maxRequestSize();
        this.fileSizeThreshold = annotation.fileSizeThreshold();
    }
    public String getLocation() {
        return this.location;
    }
    public long getMaxFileSize() {
        return this.maxFileSize;
    }
    public long getMaxRequestSize() {
        return this.maxRequestSize;
    }
    public int getFileSizeThreshold() {
        return this.fileSizeThreshold;
    }
}
