package org.apache.catalina.valves;
import java.util.Date;
static final class AbstractAccessLogValve$2 extends ThreadLocal<Date> {
    @Override
    protected Date initialValue() {
        return new Date();
    }
}
