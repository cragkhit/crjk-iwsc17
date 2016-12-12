package org.apache.tomcat.util.net.openssl;
import java.util.NoSuchElementException;
import java.util.Enumeration;
private static final class EmptyEnumeration implements Enumeration<byte[]> {
    @Override
    public boolean hasMoreElements() {
        return false;
    }
    @Override
    public byte[] nextElement() {
        throw new NoSuchElementException();
    }
}
