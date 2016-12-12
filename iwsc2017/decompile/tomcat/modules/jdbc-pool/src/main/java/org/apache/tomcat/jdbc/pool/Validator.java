// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.sql.Connection;

public interface Validator
{
    boolean validate(Connection p0, int p1);
}
