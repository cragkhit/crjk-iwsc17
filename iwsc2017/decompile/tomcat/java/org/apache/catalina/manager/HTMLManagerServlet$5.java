package org.apache.catalina.manager;
import org.apache.catalina.Session;
import org.apache.catalina.manager.util.BaseSessionComparator;
class HTMLManagerServlet$5 extends BaseSessionComparator<Boolean> {
    @Override
    public Comparable<Boolean> getComparableObject ( final Session session ) {
        return session.getSession().isNew();
    }
}
