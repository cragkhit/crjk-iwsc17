package org.apache.catalina.manager;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.catalina.Session;
import java.util.Date;
import org.apache.catalina.manager.util.BaseSessionComparator;
class HTMLManagerServlet$9 extends BaseSessionComparator<Date> {
    @Override
    public Comparable<Date> getComparableObject ( final Session session ) {
        return new Date ( SessionUtils.getInactiveTimeForSession ( session ) );
    }
}
