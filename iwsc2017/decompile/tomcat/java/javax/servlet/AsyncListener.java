package javax.servlet;
import java.io.IOException;
import java.util.EventListener;
public interface AsyncListener extends EventListener {
    void onComplete ( AsyncEvent p0 ) throws IOException;
    void onTimeout ( AsyncEvent p0 ) throws IOException;
    void onError ( AsyncEvent p0 ) throws IOException;
    void onStartAsync ( AsyncEvent p0 ) throws IOException;
}
