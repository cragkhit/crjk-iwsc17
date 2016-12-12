// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.concurrent.CountDownLatch;

protected class ExchangeCountDownLatch<T> extends CountDownLatch
{
    protected volatile T item;
    
    public ExchangeCountDownLatch(final int i) {
        super(i);
    }
    
    public T getItem() {
        return this.item;
    }
    
    public void setItem(final T item) {
        this.item = item;
    }
}
