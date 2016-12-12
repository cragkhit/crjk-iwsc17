package org.jfree.chart.util;
import java.awt.image.BufferedImage;
public interface ShadowGenerator {
    BufferedImage createDropShadow ( BufferedImage p0 );
    int calculateOffsetX();
    int calculateOffsetY();
}
