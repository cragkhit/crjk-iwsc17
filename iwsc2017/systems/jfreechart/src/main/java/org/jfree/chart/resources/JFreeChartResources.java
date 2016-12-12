

package org.jfree.chart.resources;

import java.util.ListResourceBundle;


public class JFreeChartResources extends ListResourceBundle {


    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }


    private static final Object[][] CONTENTS = {
        {"project.name",      "JFreeChart"},
        {"project.version",   "1.0.18"},
        {"project.info",      "http://www.jfree.org/jfreechart/index.html"},
        {
            "project.copyright",
            "(C)opyright 2000-2016, by Object Refinery Limited and Contributors"
        }
    };

}
