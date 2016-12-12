package org.apache.tomcat;
public interface JarScanFilter {
    boolean check ( JarScanType jarScanType, String jarName );
}
