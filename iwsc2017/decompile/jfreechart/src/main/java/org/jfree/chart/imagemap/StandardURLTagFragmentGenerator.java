package org.jfree.chart.imagemap;
public class StandardURLTagFragmentGenerator implements URLTagFragmentGenerator {
    @Override
    public String generateURLFragment ( final String urlText ) {
        return " href=\"" + urlText + "\"";
    }
}
