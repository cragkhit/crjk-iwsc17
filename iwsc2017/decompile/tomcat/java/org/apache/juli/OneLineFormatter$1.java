package org.apache.juli;
import java.util.Map;
import java.util.LinkedHashMap;
static final class OneLineFormatter$1 extends ThreadLocal<LinkedHashMap<Integer, String>> {
    @Override
    protected LinkedHashMap<Integer, String> initialValue() {
        return new LinkedHashMap<Integer, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry ( final Map.Entry<Integer, String> eldest ) {
                return this.size() > 10000;
            }
        };
    }
}
