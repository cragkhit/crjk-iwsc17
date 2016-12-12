package javax.servlet;
import java.io.IOException;
public interface ReadListener extends java.util.EventListener {
    public abstract void onDataAvailable() throws IOException;
    public abstract void onAllDataRead() throws IOException;
    public abstract void onError ( java.lang.Throwable throwable );
}
