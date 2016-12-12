package javax.servlet;
import javax.servlet.annotation.MultipartConfig;
public class MultipartConfigElement {
    private final String location;
    private final long maxFileSize;
    private final long maxRequestSize;
    private final int fileSizeThreshold;
    public MultipartConfigElement ( String location ) {
        if ( location != null ) {
            this.location = location;
        } else {
            this.location = "";
        }
        this.maxFileSize = -1;
        this.maxRequestSize = -1;
        this.fileSizeThreshold = 0;
    }
    public MultipartConfigElement ( String location, long maxFileSize,
                                    long maxRequestSize, int fileSizeThreshold ) {
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
    public MultipartConfigElement ( MultipartConfig annotation ) {
        location = annotation.location();
        maxFileSize = annotation.maxFileSize();
        maxRequestSize = annotation.maxRequestSize();
        fileSizeThreshold = annotation.fileSizeThreshold();
    }
    public String getLocation() {
        return location;
    }
    public long getMaxFileSize() {
        return maxFileSize;
    }
    public long getMaxRequestSize() {
        return maxRequestSize;
    }
    public int getFileSizeThreshold() {
        return fileSizeThreshold;
    }
}
