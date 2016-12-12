package org.apache.catalina.manager;
import org.apache.catalina.Session;
import org.apache.catalina.manager.util.BaseSessionComparator;
class HTMLManagerServlet$2 extends BaseSessionComparator<String> {
    @Override
    public Comparable<String> getComparableObject ( final Session session ) {
        return session.getId();
    }
}
