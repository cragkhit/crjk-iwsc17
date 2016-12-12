package org.apache.catalina.valves;
import java.util.Locale;
static final class AbstractAccessLogValve$1 extends ThreadLocal<DateFormatCache> {
    @Override
    protected DateFormatCache initialValue() {
        return new DateFormatCache ( 60, Locale.getDefault(), AbstractAccessLogValve.access$500() );
    }
}
