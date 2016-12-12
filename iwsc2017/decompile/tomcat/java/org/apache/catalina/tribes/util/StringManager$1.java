package org.apache.catalina.tribes.util;
import java.util.Map;
import java.util.Locale;
import java.util.LinkedHashMap;
static final class StringManager$1 extends LinkedHashMap<Locale, StringManager> {
    private static final long serialVersionUID = 1L;
    @Override
    protected boolean removeEldestEntry ( final Map.Entry<Locale, StringManager> eldest ) {
        return this.size() > StringManager.access$000() - 1;
    }
}
