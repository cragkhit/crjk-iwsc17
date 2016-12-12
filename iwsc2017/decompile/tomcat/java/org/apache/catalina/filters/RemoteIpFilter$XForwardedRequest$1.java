package org.apache.catalina.filters;
import java.util.Locale;
import java.text.SimpleDateFormat;
static final class RemoteIpFilter$XForwardedRequest$1 extends ThreadLocal<SimpleDateFormat[]> {
    @Override
    protected SimpleDateFormat[] initialValue() {
        return new SimpleDateFormat[] { new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEE MMMM d HH:mm:ss yyyy", Locale.US ) };
    }
}
