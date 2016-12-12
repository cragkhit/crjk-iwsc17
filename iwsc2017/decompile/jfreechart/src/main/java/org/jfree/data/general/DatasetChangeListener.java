package org.jfree.data.general;
import java.util.EventListener;
public interface DatasetChangeListener extends EventListener {
    void datasetChanged ( DatasetChangeEvent p0 );
}
