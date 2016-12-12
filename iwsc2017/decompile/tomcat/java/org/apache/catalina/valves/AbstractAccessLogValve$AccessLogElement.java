package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected interface AccessLogElement {
    void addElement ( CharArrayWriter p0, Date p1, Request p2, Response p3, long p4 );
}
