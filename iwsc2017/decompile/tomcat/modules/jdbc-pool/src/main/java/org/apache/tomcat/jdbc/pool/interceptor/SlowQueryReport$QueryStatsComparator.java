// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import javax.management.openmbean.SimpleType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.management.openmbean.OpenType;
import java.util.Comparator;

private static class QueryStatsComparator implements Comparator<QueryStats>
{
    @Override
    public int compare(final QueryStats stats1, final QueryStats stats2) {
        return Long.compare(handleZero(stats1.lastInvocation), handleZero(stats2.lastInvocation));
    }
    
    private static long handleZero(final long value) {
        return (value == 0L) ? Long.MAX_VALUE : value;
    }
}
