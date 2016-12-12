package javax.servlet;
import java.io.IOException;
public interface WriteListener extends java.util.EventListener {
    public void onWritePossible() throws IOException;
    public void onError ( java.lang.Throwable throwable );
}
