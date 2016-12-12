package org.apache.catalina.manager;
import org.apache.catalina.Session;
import org.apache.catalina.manager.util.BaseSessionComparator;
class HTMLManagerServlet$4 extends BaseSessionComparator<Integer> {
    @Override
    public Comparable<Integer> getComparableObject ( final Session session ) {
        return session.getMaxInactiveInterval();
    }
}
