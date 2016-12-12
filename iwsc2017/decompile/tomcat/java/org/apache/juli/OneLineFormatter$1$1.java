package org.apache.juli;
import java.util.Map;
import java.util.LinkedHashMap;
class OneLineFormatter$1$1 extends LinkedHashMap<Integer, String> {
    private static final long serialVersionUID = 1L;
    @Override
    protected boolean removeEldestEntry ( final Map.Entry<Integer, String> eldest ) {
        return this.size() > 10000;
    }
}
