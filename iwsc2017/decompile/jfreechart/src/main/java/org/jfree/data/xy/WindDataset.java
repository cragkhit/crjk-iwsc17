package org.jfree.data.xy;
public interface WindDataset extends XYDataset {
    Number getWindDirection ( int p0, int p1 );
    Number getWindForce ( int p0, int p1 );
}
